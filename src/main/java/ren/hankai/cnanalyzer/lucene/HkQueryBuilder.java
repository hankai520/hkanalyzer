
package ren.hankai.cnanalyzer.lucene;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.core.Segmentator;
import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lucene 查询构造器。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 2:17:31 PM
 */
public class HkQueryBuilder {

  private static final Logger logger = LoggerFactory.getLogger(HkQueryBuilder.class);

  /**
   * 是否采用最大且分词策略。
   */
  public static boolean isLongerTextOnly = false;

  /*
   * 搜索关键字的缓存。
   */
  private static ThreadLocal<Map<String, QueryBranch>> keywords = new ThreadLocal<>();

  /**
   * 将多个查询合并为一个查询（查询列表中各个查询在组合时的关系为逻辑与）。
   *
   * @param queries 查询列表
   * @return 查询
   * @author hankai
   * @since Jan 19, 2017 11:05:48 AM
   */
  private static Query mergeQueries(List<Query> queries) {
    if (queries.size() == 0) {
      return null;
    } else if (queries.size() == 1) {
      return queries.get(0);
    } else {
      final BooleanQuery.Builder builder = new BooleanQuery.Builder();
      for (final Query query : queries) {
        builder.add(query, Occur.MUST);
      }
      return builder.build();
    }
  }

  /**
   * 获取查询关键字的缓存。
   *
   * @return 关键字缓存
   * @author hankai
   * @since Jan 19, 2017 11:08:43 AM
   */
  private static Map<String, QueryBranch> getKeywords() {
    Map<String, QueryBranch> cache = keywords.get();
    if (cache == null) {
      cache = new HashMap<>(4);
      keywords.set(cache);
    }
    return cache;
  }

  /**
   * 获取查询关键字对应的博弈树。
   *
   * @param query 关键字
   * @return 博弈树
   * @author hankai
   * @since Jan 19, 2017 11:27:22 AM
   */
  private static QueryBranch getTokenBranch(String query) {
    final Map<String, QueryBranch> keywordCache = getKeywords();
    return keywordCache.get(query);
  }

  /**
   * 缓存查询关键字的博弈树。
   *
   * @param query 关键字
   * @param tb 博弈树
   * @author hankai
   * @since Jan 19, 2017 11:28:05 AM
   */
  private static void cacheTokenBranch(String query, QueryBranch tb) {
    final Map<String, QueryBranch> keywordCache = getKeywords();
    if (keywordCache.size() > 1200) {
      keywordCache.clear();
    }
    keywordCache.put(query, tb);
  }

  /**
   * 将查询条件解析为单个字段的查询对象。
   *
   * @param field 字段
   * @param query 关键字（间隔字符，例如：空格、退格、换行、制表符等会被清除）
   * @return 查询对象
   * @author hankai
   * @since Jan 19, 2017 11:28:52 AM
   */
  public static Query build(String field, String query) {
    Objects.requireNonNull(field, "Field cannot be null.");
    if (StringUtils.isEmpty(query)) {
      return new TermQuery(new Term(field));
    }
    query = CharacterUtil.removeSpeceCharacters(query);
    QueryBranch root = getTokenBranch(query);
    if (root == null) {
      root = new QueryBranch(null);
      final StringReader input = new StringReader(query);
      final Segmentator segmentator = new Segmentator(input, isLongerTextOnly);
      try {
        Lexeme lexeme = segmentator.next();
        while (lexeme != null) {
          root.adopt(lexeme);
          lexeme = segmentator.next();
        }
      } catch (final IOException ex) {
        logger.error("Failed to segment query keyword: " + query, ex);
      }
      cacheTokenBranch(query, root);
    }
    return mergeQueries(root.toQueries(field));
  }

  /**
   * 构建关键字匹配多个字段的查询对象（多个字段的匹配条件之间的关系为逻辑或）。
   *
   * @param fields 字段数组
   * @param query 关键字
   * @return 查询对象
   * @author hankai
   * @since Jan 19, 2017 11:34:04 AM
   */
  public static Query build(String[] fields, String query) {
    return build(fields, new String[] {query}, new Occur[] {});
  }

  /**
   * 构建关键字匹配多个字段的查询对象。
   *
   * @param fields 要匹配的字段
   * @param query 关键字
   * @param occurs 为多个字段构造的查询条件之间的逻辑关系（默认为逻辑或）
   * @return 查询对象
   * @author hankai
   * @since Jan 19, 2017 11:45:06 AM
   */
  public static Query build(String[] fields, String[] query, Occur[] occurs) {
    Objects.requireNonNull(fields, "Fields cannot be null.");
    Objects.requireNonNull(occurs, "Flags cannot be null.");
    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    Occur lastOccur = Occur.SHOULD;
    String lastQuery = "";
    for (int i = 0; i < fields.length; i++) {
      final String field = fields[i];
      final Occur occur = i < occurs.length ? occurs[i] : lastOccur;
      lastOccur = occur;
      final String keyword = i < query.length ? query[i] : lastQuery;
      lastQuery = keyword;
      if (StringUtils.isNotEmpty(field) && StringUtils.isNotEmpty(keyword)) {
        final Query partQuery = build(field, keyword);
        if (((partQuery != null) &&
            (!(partQuery instanceof BooleanQuery)
                || (((BooleanQuery) partQuery).clauses().size() > 0)))) {
          builder.add(partQuery, occur);
        }
      }
    }
    return builder.build();
  }

  /**
   * 用于处理分词产生歧义时，容纳歧义词元的博弈树。假设对搜索关键词：”飞流直下三千尺“ 进行分词，会产生 [飞流直下三千尺，飞流，三千]
   * 这样一个分词集。假设这个关键词对应有一个树结构，则“飞流直下三千尺” 会变为树干，“飞流” 和 “三千” 会变为这个树干的两个分支， 由 其构成的查询条件为 { “飞流直下三千尺” &&
   * ("飞流" || "三千")}。树干与树干之间为逻辑与，则在构造查询，遍历树干和树枝时，逻辑与会与逻辑或交替连接，形成一个复杂的查询条件。
   * 这样查询时，能够将关键词中的近义词、歧义词等都匹配到，使得查询经度更高。
   *
   * @author hankai
   * @version 1.0.0
   * @since Jan 19, 2017 1:43:22 PM
   */
  private static class QueryBranch {

    /* 不适用 0 这种特殊值，因为非包装类型会有默认值 */
    private static final int ADOPTED = -1; // 标记目标词元为歧义词，可以被存入当前博弈树节点（博弈成功）。
    private static final int HAND_OVER = 1; // 标记目标词元首字符索引位置超出了右边界，应该将当前决策权交由下一个树节点（博弈失败）。

    private int rightBorder; // 当前节点的右边界（边界由当前节点所接受的词元进行拓展，接受后，右边界被拓展为词元末字符的位置索引）
    private Lexeme lexeme; // 当前节点主词元
    private final List<QueryBranch> childBranches = new ArrayList<>(1); // 与当前节点主词元有关的歧义词元。
    private QueryBranch nextBranch; // 下一个决策节点（相当于博弈中的选手）。

    QueryBranch(Lexeme lexeme) {
      if (lexeme != null) {
        this.lexeme = lexeme;
        rightBorder = lexeme.getEndPosition();// 正向分词，不考虑左边界
      }
    }

    public Lexeme getLexeme() {
      return lexeme;
    }

    @Override
    public int hashCode() {
      if (lexeme == null) {
        return 0;
      } else {
        return lexeme.hashCode() * 37;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if ((o != null) && (o instanceof QueryBranch)) {
        final QueryBranch other = (QueryBranch) o;
        if ((lexeme != null) && (other.getLexeme() != null)) {
          return lexeme.equals(other.getLexeme());
        }
      }
      return false;
    }

    /**
     * 将当前词元博弈树中的节点的词元组织为查询条件（同节点为逻辑或，异节点为逻辑与，逻辑或和逻辑与交替连接）。
     *
     * @param fieldName 要匹配的字段名
     * @return 查询条件列表
     * @author hankai
     * @since Jan 19, 2017 2:20:29 PM
     */
    private List<Query> toQueries(String fieldName) {
      final List<Query> queries = new ArrayList<>(1);
      if (lexeme != null) {
        queries.add(new TermQuery(new Term(fieldName, lexeme.getText())));
      }
      if ((childBranches.size() > 0)) {
        if (childBranches.size() == 1) {
          final Query onlyOneQuery = mergeQueries(childBranches.get(0).toQueries(fieldName));
          if (onlyOneQuery != null) {
            queries.add(onlyOneQuery);
          }
        } else {
          final BooleanQuery.Builder builder = new BooleanQuery.Builder();
          for (final QueryBranch childBranch : childBranches) {
            final Query childQuery = mergeQueries(childBranch.toQueries(fieldName));
            if (childQuery != null) {
              builder.add(childQuery, Occur.SHOULD);
            }
          }
          final BooleanQuery orQuery = builder.build();
          if (orQuery.clauses().size() > 0) {
            queries.add(orQuery);
          }
        }
      }
      if (nextBranch != null) {
        queries.addAll(nextBranch.toQueries(fieldName));
      }
      return queries;
    }

    /**
     * 检查目标词元是否可以被作为相关词被存储到当前节点或当前节点的相邻节点。
     *
     * @param lexeme 目标词元
     * @return 是否可以被当作相关词
     * @author hankai
     * @since Jan 19, 2017 2:51:58 PM
     */
    private boolean adopt(Lexeme lexeme) {
      final int acceptType = checkAdoptability(lexeme);
      if (acceptType == ADOPTED) {
        if (childBranches.isEmpty()) {
          childBranches.add(new QueryBranch(lexeme));
        } else {
          for (final QueryBranch childBranch : childBranches) {
            if (childBranch.adopt(lexeme)) {
              childBranches.add(new QueryBranch(lexeme));
              break;
            }
          }
        }
        if (lexeme.getEndPosition() > rightBorder) {
          rightBorder = lexeme.getEndPosition();
        }
      } else if (acceptType == HAND_OVER) {
        if (nextBranch == null) {
          nextBranch = new QueryBranch(null);
        }
        nextBranch.adopt(lexeme);
      } else {
        return false;
      }
      return true;
    }

    /**
     * 检查目标词元是否能作为一个相关词（歧义词）存入当前博弈树节点（条件：目标词元位于当前节点边界内，且不与其他词元相交）。
     *
     * @param lexeme 目标词元
     * @return 是否可以作为相关词（返回枚举值，TONEXT: 交由下一节点决策；ACCEPTED: 可以作为相关词）
     * @author hankai
     * @since Jan 19, 2017 2:25:06 PM
     */
    private int checkAdoptability(Lexeme lexeme) {
      Objects.requireNonNull(lexeme, "Lexeme cannot be null!");
      int acceptType = 0;
      if (null == this.lexeme) { // 如果当前节点是一个根节点
        if ((rightBorder > 0) && (lexeme.getBeginPosition() >= rightBorder)) {
          // 当前节点含有至少1个相关词，且目标词元位置在当前节点的边界外，则交由下一节点进行决策。
          acceptType = HAND_OVER;
        } else {
          // 如果当前节点不含有任何相关词，或者，目标词元在当前节点边界内，则认为目标词元可能是一个关联词（歧义词）。
          acceptType = ADOPTED;
        }
      } else {
        if ((lexeme.getBeginPosition() >= lexeme.getEndPosition())
            && (lexeme.getBeginPosition() < rightBorder)) {
          // 如果目标词元与当前节点主词元不相交，但位于当前节点边界内，则认为目标词元可能是一个关联词（歧义词）。
          acceptType = ADOPTED;
        } else {
          // 如果目标词元位于当前节点边界外，则交由下一节点进行决策。
          acceptType = HAND_OVER;
        }
      }
      return acceptType;
    }
  }
}
