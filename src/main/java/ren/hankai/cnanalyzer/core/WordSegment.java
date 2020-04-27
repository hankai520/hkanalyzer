package ren.hankai.cnanalyzer.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 词典树节点。存储从词包中获取的词语，并按照单个文字组织成树形结构。进行单字匹配或整词匹配时， 按树节点所存储的字符依次匹配词语中各个单字，直到找到能匹配的词语。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 18, 2017 9:47:31 AM
 */
public class WordSegment {

  // 存储当前节点的单个字符
  private final Character character;
  // 存储当前节点之后可能与当前节点匹配成词的子节点
  private final Map<Character, WordSegment> children = new HashMap<>(6, 0.8f);
  // 标记从根节点到当前节点的字符序列是否是一个完整的词
  private boolean isCompleteWord = false;

  public WordSegment(final Character nodeChar) {
    Objects.requireNonNull(nodeChar, "Node character must not be null!");
    character = nodeChar;
  }

  /**
   * 获取词典节点对应的单个字。
   *
   * @return 节点对应的字
   * @author hankai
   * @since Jan 18, 2017 9:58:14 AM
   */
  public Character getCharacter() {
    return character;
  }

  /**
   * 是否有子节点的字可能与当前节点的字构成词。
   *
   * @return 是否有子节点
   * @author hankai
   * @since Jan 18, 2017 10:01:51 AM
   */
  public boolean hasChildren() {
    return children.size() > 0;
  }

  /**
   * 在字典树上搜索字符序列，查看是否有词与之匹配（单字匹配，即按字符序列指定位置的字符进行匹配）。
   *
   * @param charArray 字符序列
   * @param begin 要匹配的字符在序列中的位置
   * @param length 要匹配的字符个数
   * @return 命中信息
   * @author hankai
   * @since Jan 18, 2017 9:34:38 AM
   */
  public Hit match(final char[] charArray, final int begin, final int length) {
    return match(charArray, begin, length, null);
  }

  /**
   * 在字典树上搜索字符序列，查看是否有词与之匹配（单字匹配，即按字符序列指定位置的字符进行匹配）。
   *
   * @param charArray 字符序列
   * @param begin 要匹配的字符在序列中的位置
   * @param searchHit 用于在递归搜索时，保存命中信息
   * @return 命中信息
   * @author hankai
   * @since Jan 18, 2017 9:26:24 AM
   */
  public Hit match(final char[] charArray, final int begin, final Hit searchHit) {
    return match(charArray, begin, -1, searchHit);
  }

  /**
   * 在字典树上搜索字符序列，查看是否有词与之匹配。
   *
   * @param charArray 字符序列
   * @param begin 要匹配的字符在序列中的位置
   * @param length 要匹配的字符的长度（大于1:整词匹配，等于1:单字匹配）
   * @param searchHit 用于在递归搜索时，保存命中信息
   * @return 命中信息
   * @author hankai
   * @since Jan 18, 2017 9:22:01 AM
   */
  public Hit match(final char[] charArray, final int begin, final int length, Hit searchHit) {
    if (searchHit == null) {
      searchHit = new Hit();
      searchHit.setBegin(begin);
    } else {
      searchHit.setUnmatch();
    }
    searchHit.setEnd(begin);
    final Character keyChar = new Character(charArray[begin]);
    final Map<Character, WordSegment> segmentMap = children;
    WordSegment ds = null;
    if (segmentMap != null) {
      ds = segmentMap.get(keyChar);
    }
    if (ds != null) {
      if (length > 1) {
        return ds.match(charArray, begin + 1, length - 1, searchHit);
      }
      if (ds.isCompleteWord) {
        searchHit.setMatch();
      }
      if (ds.hasChildren()) {
        searchHit.setPrefix();
        searchHit.setMatchedWord(ds);
      }
      return searchHit;
    }
    return searchHit;
  }

  /**
   * 将词填充到词典树。
   *
   * @param charArray 词对应的字符序列
   * @author hankai
   * @since Jan 17, 2017 4:21:48 PM
   */
  public void fillSegment(final char[] charArray) {
    this.fillSegment(charArray, 0, charArray.length);
  }

  /**
   * 将词填充到词典树（将词按单字转换为树种的一条路径）。
   *
   * @param charArray 词对应的字符序列
   * @param begin 从字符序列的第几个字符开始填充（值大于等于0）
   * @param length 要填充几个字符
   * @author hankai
   * @since Jan 18, 2017 10:03:07 AM
   */
  public synchronized void fillSegment(final char[] charArray, final int begin, final int length) {
    final Character beginChar = new Character(charArray[begin]);
    final WordSegment ds = findSegment(beginChar);
    if (length > 1) {
      ds.fillSegment(charArray, begin + 1, length - 1);
    } else if (length == 1) {
      ds.isCompleteWord = true;
    }
  }

  /**
   * 查找本节点下对应的keyChar的segment 如果没有找到，则创建新的segment
   *
   * @param keyChar
   * @return
   */
  private WordSegment findSegment(final Character keyChar) {
    final Map<Character, WordSegment> segmentMap = children;
    WordSegment ds = segmentMap.get(keyChar);
    if (ds == null) {
      ds = new WordSegment(keyChar);
      segmentMap.put(keyChar, ds);
    }
    return ds;
  }
}
