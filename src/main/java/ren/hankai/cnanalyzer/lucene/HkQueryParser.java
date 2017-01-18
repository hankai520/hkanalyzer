
package ren.hankai.cnanalyzer.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import ren.hankai.cnanalyzer.core.Segmentator;
import ren.hankai.cnanalyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lucene 查询构造器。
 * 
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 2:17:31 PM
 */
public class HkQueryParser {
  // 查询关键字解析缓存线程本地变量
  private static ThreadLocal<Map<String, TokenBranch>> keywordCacheThreadLocal =
      new ThreadLocal<>();

  /**
   * 优化query队列 减少Query表达式的嵌套
   *
   * @param queries
   * @return
   */
  private static Query optimizeQueries(List<Query> queries) {
    // 生成当前branch 的完整query
    if (queries.size() == 0) {
      return null;
    } else if (queries.size() == 1) {
      return queries.get(0);
    } else {
      final BooleanQuery.Builder builder = new BooleanQuery.Builder();
      for (final Query q : queries) {
        builder.add(q, Occur.MUST);
      }
      return builder.build();
    }
  }

  /**
   * 获取线程本地的解析缓存
   *
   * @return
   */
  private static Map<String, TokenBranch> getTheadLocalCache() {
    Map<String, TokenBranch> keywordCache = keywordCacheThreadLocal.get();
    if (keywordCache == null) {
      keywordCache = new HashMap<>(4);
      keywordCacheThreadLocal.set(keywordCache);
    }
    return keywordCache;
  }

  /**
   * 缓存解析结果的博弈树
   *
   * @param query
   * @return
   */
  private static TokenBranch getCachedTokenBranch(String query) {
    final Map<String, TokenBranch> keywordCache = getTheadLocalCache();
    return keywordCache.get(query);
  }

  /**
   * 缓存解析结果的博弈树
   *
   * @param query
   * @return
   */
  private static void cachedTokenBranch(String query, TokenBranch tb) {
    final Map<String, TokenBranch> keywordCache = getTheadLocalCache();
    keywordCache.put(query, tb);
  }


  /**
   * 单连续字窜（不带空格符）单Field查询分析
   *
   * @param field
   * @param query
   * @return
   */
  public static Query parse(String field, String query) {
    if (field == null) {
      throw new IllegalArgumentException("parameter \"field\" is null");
    }

    if ((query == null) || "".equals(query.trim())) {
      return new TermQuery(new Term(field));
    }

    // 从缓存中取出已经解析的query生产的TokenBranch
    TokenBranch root = getCachedTokenBranch(query);
    if (root != null) {
      return optimizeQueries(root.toQueries(field));
    } else {
      // System.out.println(System.currentTimeMillis());
      root = new TokenBranch(null);
      // 对查询条件q进行分词
      final StringReader input = new StringReader(query.trim());
      final Segmentator segmentator = new Segmentator(input);
      try {
        for (Lexeme lexeme = segmentator.next(); lexeme != null; lexeme = segmentator.next()) {
          // 处理词元分支
          root.accept(lexeme);
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
      // 缓存解析结果的博弈树
      cachedTokenBranch(query, root);
      return optimizeQueries(root.toQueries(field));
    }
  }

  /**
   * 多Field,单条件查询分析
   *
   * @param fields -- Document fields name
   * @param query -- keyword
   * @return Query 查询逻辑对象
   * @throws IOException
   */
  public static Query parseMultiField(String[] fields, String query) throws IOException {
    if (fields == null) {
      throw new IllegalArgumentException("parameter \"fields\" is null");
    }
    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (final String field : fields) {
      if (field != null) {
        final Query partQuery = parse(field, query);
        if (((partQuery != null) &&
            (!(partQuery instanceof BooleanQuery)
                || (((BooleanQuery) partQuery).clauses().size() > 0)))) {
          builder.add(partQuery, Occur.SHOULD);
        }
      }
    }
    return builder.build();
  }

  /**
   * 多Field,单条件,多Occur查询分析
   *
   * @param fields -- Document fields name
   * @param query -- keyword
   * @param flags -- BooleanClause
   * @return Query 查询逻辑对象
   * @throws IOException
   */
  public static Query parseMultiField(String[] fields, String query, BooleanClause.Occur[] flags)
      throws IOException {
    if (fields == null) {
      throw new IllegalArgumentException("parameter \"fields\" is null");
    }
    if (flags == null) {
      throw new IllegalArgumentException("parameter \"flags\" is null");
    }

    if (flags.length != fields.length) {
      throw new IllegalArgumentException("flags.length != fields.length");
    }

    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i] != null) {
        final Query partQuery = parse(fields[i], query);
        if (((partQuery != null) &&
            (!(partQuery instanceof BooleanQuery)
                || (((BooleanQuery) partQuery).clauses().size() > 0)))) {
          builder.add(partQuery, flags[i]);
        }
      }
    }
    return builder.build();
  }

  /**
   * 多Field多条件查询分析
   *
   * @param fields
   * @param queries
   * @return Query 查询逻辑对象
   * @throws IOException
   */
  public static Query parseMultiField(String[] fields, String[] queries) throws IOException {
    if (fields == null) {
      throw new IllegalArgumentException("parameter \"fields\" is null");
    }
    if (queries == null) {
      throw new IllegalArgumentException("parameter \"queries\" is null");
    }
    if (queries.length != fields.length) {
      throw new IllegalArgumentException("queries.length != fields.length");
    }
    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i] != null) {
        final Query partQuery = parse(fields[i], queries[i]);
        if (((partQuery != null) &&
            (!(partQuery instanceof BooleanQuery)
                || (((BooleanQuery) partQuery).clauses().size() > 0)))) {
          builder.add(partQuery, Occur.SHOULD);
        }
      }
    }
    return builder.build();
  }

  /**
   * 多Field,多条件,多Occur查询分析
   *
   * @param fields
   * @param queries
   * @param flags
   * @return Query 查询逻辑对象
   * @throws IOException
   */
  public static Query parseMultiField(String[] fields, String[] queries,
      BooleanClause.Occur[] flags) throws IOException {
    if (fields == null) {
      throw new IllegalArgumentException("parameter \"fields\" is null");
    }
    if (queries == null) {
      throw new IllegalArgumentException("parameter \"queries\" is null");
    }
    if (flags == null) {
      throw new IllegalArgumentException("parameter \"flags\" is null");
    }

    if (!((queries.length == fields.length) && (queries.length == flags.length))) {
      throw new IllegalArgumentException(
          "queries, fields, and flags array have have different length");
    }

    final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i] != null) {
        final Query partQuery = parse(fields[i], queries[i]);
        if (((partQuery != null) &&
            (!(partQuery instanceof BooleanQuery)
                || (((BooleanQuery) partQuery).clauses().size() > 0)))) {
          builder.add(partQuery, flags[i]);
        }
      }
    }
    return builder.build();
  }

  /**
   * 词元分支 当分词出现歧义时，采用词元分支容纳不同的歧义组合
   *
   * @author 林良益
   *
   */
  private static class TokenBranch {

    private static final int REFUSED = -1;
    private static final int ACCEPTED = 0;
    private static final int TONEXT = 1;

    // 词元分支左边界
    private int leftBorder;
    // 词元分支右边界
    private int rightBorder;
    // 当前分支主词元
    private Lexeme lexeme;
    // 当前分支可并入的词元分支
    private List<TokenBranch> acceptedBranchs;
    // 当前分支的后一个相邻分支
    private TokenBranch nextBranch;

    TokenBranch(Lexeme lexeme) {
      if (lexeme != null) {
        this.lexeme = lexeme;
        // 初始化branch的左右边界
        leftBorder = lexeme.getBeginPosition();
        rightBorder = lexeme.getEndPosition();
      }
    }

    public int getLeftBorder() {
      return leftBorder;
    }

    public int getRightBorder() {
      return rightBorder;
    }

    public Lexeme getLexeme() {
      return lexeme;
    }

    public List<TokenBranch> getAcceptedBranchs() {
      return acceptedBranchs;
    }

    public TokenBranch getNextBranch() {
      return nextBranch;
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
      if (o == null) {
        return false;
      }
      if (this == o) {
        return true;
      }
      if (o instanceof TokenBranch) {
        final TokenBranch other = (TokenBranch) o;
        if ((lexeme == null) ||
            (other.getLexeme() == null)) {
          return false;
        } else {
          return lexeme.equals(other.getLexeme());
        }
      } else {
        return false;
      }
    }

    /**
     * 组合词元分支
     *
     * @param _lexeme
     * @return 返回当前branch能否接收词元对象
     */
    boolean accept(Lexeme _lexeme) {

      /*
       * 检查新的lexeme 对当前的branch 的可接受类型 acceptType : REFUSED 不能接受 acceptType : ACCEPTED 接受 acceptType
       * : TONEXT 由相邻分支接受
       */
      final int acceptType = checkAccept(_lexeme);
      switch (acceptType) {
        case REFUSED:
          // REFUSE 情况
          return false;

        case ACCEPTED:
          if (acceptedBranchs == null) {
            // 当前branch没有子branch，则添加到当前branch下
            acceptedBranchs = new ArrayList<>(2);
            acceptedBranchs.add(new TokenBranch(_lexeme));
          } else {
            boolean acceptedByChild = false;
            // 当前branch拥有子branch，则优先由子branch接纳
            for (final TokenBranch childBranch : acceptedBranchs) {
              acceptedByChild = childBranch.accept(_lexeme) || acceptedByChild;
            }
            // 如果所有的子branch不能接纳，则由当前branch接纳
            if (!acceptedByChild) {
              acceptedBranchs.add(new TokenBranch(_lexeme));
            }
          }
          // 设置branch的最大右边界
          if (_lexeme.getEndPosition() > rightBorder) {
            rightBorder = _lexeme.getEndPosition();
          }
          break;

        case TONEXT:
          // 把lexeme放入当前branch的相邻分支
          if (nextBranch == null) {
            // 如果还没有相邻分支，则建立一个不交叠的分支
            nextBranch = new TokenBranch(null);
          }
          nextBranch.accept(_lexeme);
          break;
      }

      return true;
    }

    /**
     * 将分支数据转成Query逻辑
     *
     * @return
     */
    List<Query> toQueries(String fieldName) {
      final List<Query> queries = new ArrayList<>(1);
      // 生成当前branch 的query
      if (lexeme != null) {
        queries.add(new TermQuery(new Term(fieldName, lexeme.getText())));
      }
      // 生成child branch 的query
      if ((acceptedBranchs != null) && (acceptedBranchs.size() > 0)) {
        if (acceptedBranchs.size() == 1) {
          final Query onlyOneQuery = optimizeQueries(acceptedBranchs.get(0).toQueries(fieldName));
          if (onlyOneQuery != null) {
            queries.add(onlyOneQuery);
          }
        } else {
          final BooleanQuery.Builder builder = new BooleanQuery.Builder();
          // final BooleanQuery orQuery = new BooleanQuery();
          for (final TokenBranch childBranch : acceptedBranchs) {
            final Query childQuery = optimizeQueries(childBranch.toQueries(fieldName));
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
      // 生成nextBranch的query
      if (nextBranch != null) {
        queries.addAll(nextBranch.toQueries(fieldName));
      }
      return queries;
    }

    /**
     * 判断指定的lexeme能否被当前的branch接受
     *
     * @param lexeme
     * @return 返回接受的形式
     */
    private int checkAccept(Lexeme _lexeme) {
      int acceptType = 0;

      if (_lexeme == null) {
        throw new IllegalArgumentException("parameter:lexeme is null");
      }

      if (null == lexeme) {// 当前的branch是一个不交叠（ROOT）的分支
        if ((rightBorder > 0 // 说明当前branch内至少有一个lexeme
        )
            && (_lexeme.getBeginPosition() >= rightBorder)) {
          // _lexeme 与 当前的branch不相交
          acceptType = TONEXT;
        } else {
          acceptType = ACCEPTED;
        }
      } else {// 当前的branch是一个有交叠的分支

        if (_lexeme.getBeginPosition() < lexeme.getBeginPosition()) {
          // _lexeme 的位置比 this.lexeme还靠前（这种情况不应该发生）
          acceptType = REFUSED;
        } else if ((_lexeme.getBeginPosition() >= lexeme.getBeginPosition())
            && (_lexeme.getBeginPosition() < lexeme.getEndPosition())) {
          // _lexeme 与 this.lexeme相交
          acceptType = REFUSED;
        } else if ((_lexeme.getBeginPosition() >= lexeme.getEndPosition())
            && (_lexeme.getBeginPosition() < rightBorder)) {
          // _lexeme 与 this.lexeme 不相交， 但_lexeme 与 当前的branch相交
          acceptType = ACCEPTED;
        } else {// _lexeme.getBeginPosition() >= this.rightBorder
          // _lexeme 与 当前的branch不相交
          acceptType = TONEXT;
        }
      }
      return acceptType;
    }
  }
}
