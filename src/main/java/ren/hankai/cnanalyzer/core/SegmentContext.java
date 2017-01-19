
package ren.hankai.cnanalyzer.core;

import ren.hankai.cnanalyzer.core.processor.WordProcessor;
import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * 分词上下文。由于分词采用逐字分析，需要通过上下文保存和共享信息。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 3:29:09 PM
 */
public class SegmentContext {

  // 当有多个分词匹配时，优先选择长度最大的分词结果
  private final boolean matchLongerTextOnly;
  // 记录Reader内已分析的字串总长度（累计长度）
  private int bufferOffset;
  // 最近一次读入的,可处理的字符长度
  private int availableLength;
  // 最近一次分析的字节长度
  private int lastAnalyzedLength;
  // 当前缓冲区位置指针
  private int bufferCursor;
  // 字符串读取缓冲
  private final char[] buffer;
  // 记录正在使用buffer的分词器对象 如果set中存在有分词器对象，则buffer不能进行位移操作（处于locked状态）
  private final Set<WordProcessor> activeProcessors;
  // 词元结果集，存储每次游标的移动，切分出来的词元
  private SortedLinkSet lexemeLinkSet;

  public SegmentContext(char[] buffer, boolean matchLongerTextOnly) {
    this.matchLongerTextOnly = matchLongerTextOnly;
    this.buffer = buffer;
    activeProcessors = new HashSet<>(4);
    lexemeLinkSet = new SortedLinkSet();
  }

  /**
   * 重置上下文状态。
   *
   * @author hankai
   * @since Jan 17, 2017 10:53:34 AM
   */
  public void reset() {
    activeProcessors.clear();
    lexemeLinkSet = new SortedLinkSet();
    bufferOffset = 0;
    availableLength = 0;
    lastAnalyzedLength = 0;
    bufferCursor = 0;
  }

  /**
   * 获取 buffer 字段的值。
   *
   * @return buffer 字段值
   */
  public char[] getBuffer() {
    return buffer;
  }

  /**
   * 获取当前正在处理的字符。
   *
   * @return 字符
   * @author hankai
   * @since Jan 18, 2017 2:12:35 PM
   */
  public char getCurrentCharacter() {
    return buffer[bufferCursor];
  }

  /**
   * 获取当前正在分析的字符的类型。
   *
   * @return 字符类型
   * @author hankai
   * @since Jan 18, 2017 1:35:46 PM
   */
  public int getCurrentCharType() {
    return CharacterUtil.identifyCharType(buffer[bufferCursor]);
  }

  /**
   * 标记指定文字处理器已占用缓冲区（缓冲区变为锁定状态）。
   *
   * @param processor 文字处理器
   * @author hankai
   * @since Jan 17, 2017 10:53:44 AM
   */
  public void lockBuffer(WordProcessor processor) {
    activeProcessors.add(processor);
  }

  /**
   * 标记指定文字处理器已释放缓冲区。
   *
   * @param processor 文字处理器
   * @author hankai
   * @since Jan 17, 2017 11:02:43 AM
   */
  public void unlockBuffer(WordProcessor processor) {
    activeProcessors.remove(processor);
  }

  /**
   * 是否有文字处理器占用缓冲区。
   *
   * @return 是否有文字处理器占用缓冲区
   * @author hankai
   * @since Jan 17, 2017 11:03:32 AM
   */
  public boolean isBufferLocked() {
    return activeProcessors.size() > 0;
  }

  /**
   * 缓冲区中的字符是否已全部分析过了。
   *
   * @return 缓冲区是否已全部分析完毕
   * @author hankai
   * @since Jan 18, 2017 1:46:17 PM
   */
  public boolean isBufferConsumed() {
    return bufferCursor == (availableLength - 1);
  }

  /**
   * 获取分词集合中首个词元（这个词元将被从集合中删除）。
   *
   * @return 词元
   * @author hankai
   * @since Jan 17, 2017 10:53:00 AM
   */
  public Lexeme getFirstLexeme() {
    return lexemeLinkSet.pollFirst();
  }

  /**
   * 获取分词集合中最后一个词元。
   *
   * @return 词元
   * @author hankai
   * @since Jan 17, 2017 11:03:56 AM
   */
  public Lexeme getLastLexeme() {
    return lexemeLinkSet.pollLast();
  }

  /**
   * 获取分词结果集中的最后一个词元。
   *
   * @return 词元
   * @author hankai
   * @since Jan 18, 2017 3:30:17 PM
   */
  public Lexeme peekLastLexeme() {
    if (lexemeLinkSet.tail != null) {
      return lexemeLinkSet.tail;
    }
    return null;
  }

  /**
   * 添加词元到分词集合。
   *
   * @param lexeme
   * @author hankai
   * @since Jan 17, 2017 11:04:14 AM
   */
  public void addLexeme(Lexeme lexeme) {
    if (!Dictionary.isStopWord(buffer, lexeme.getBegin(), lexeme.getLength())) {
      lexemeLinkSet.add(lexeme);
    }
  }

  /**
   * 获取分词集合大小。
   *
   * @return 分词个数
   * @author hankai
   * @since Jan 17, 2017 11:04:34 AM
   */
  public int getResultSize() {
    return lexemeLinkSet.size();
  }

  /**
   * 剔除相邻且完全包含的词元。
   *
   * @author hankai
   * @since Jan 17, 2017 11:04:52 AM
   */
  public void excludeOverlap() {
    lexemeLinkSet.excludeOverlap();
  }

  /**
   * 获取 matchLongerTextOnly 字段的值。
   *
   * @return matchLongerTextOnly 字段值
   */
  public boolean isMatchLongerTextOnly() {
    return matchLongerTextOnly;
  }

  /**
   * 获取 bufferOffset 字段的值。
   *
   * @return bufferOffset 字段值
   */
  public int getBufferOffset() {
    return bufferOffset;
  }

  /**
   * 设置 bufferOffset 字段的值。
   *
   * @param bufferOffset bufferOffset 字段的值
   */
  public void setBufferOffset(int bufferOffset) {
    this.bufferOffset = bufferOffset;
  }

  /**
   * 获取 availableLength 字段的值。
   *
   * @return availableLength 字段值
   */
  public int getAvailableLength() {
    return availableLength;
  }

  /**
   * 设置 availableLength 字段的值。
   *
   * @param availableLength availableLength 字段的值
   */
  public void setAvailableLength(int availableLength) {
    this.availableLength = availableLength;
  }

  /**
   * 获取 lastAnalyzedLength 字段的值。
   *
   * @return lastAnalyzedLength 字段值
   */
  public int getLastAnalyzedLength() {
    return lastAnalyzedLength;
  }

  /**
   * 设置 lastAnalyzedLength 字段的值。
   *
   * @param lastAnalyzedLength lastAnalyzedLength 字段的值
   */
  public void setLastAnalyzedLength(int lastAnalyzedLength) {
    this.lastAnalyzedLength = lastAnalyzedLength;
  }

  /**
   * 获取 bufferCursor 字段的值。
   *
   * @return bufferCursor 字段值
   */
  public int getBufferCursor() {
    return bufferCursor;
  }

  /**
   * 设置 bufferCursor 字段的值。
   *
   * @param bufferCursor bufferCursor 字段的值
   */
  public void setBufferCursor(int bufferCursor) {
    this.bufferCursor = bufferCursor;
  }



  /**
   * 词元有序链表（不提供随机访问）。
   *
   * @author hankai
   * @version 1.0.0
   * @since Jan 17, 2017 9:13:34 AM
   */
  private class SortedLinkSet {
    private Lexeme head = null;
    private Lexeme tail = null;
    private int size = 0;

    /**
     * 向链表集合添加词元。
     *
     * @param lexeme 词元
     * @author hankai
     * @since Jan 17, 2017 9:55:39 AM
     */
    private void add(Lexeme lexeme) {
      if (size == 0) {
        head = tail = lexeme;
        size++;
        return;
      }
      final int tailCompareResult = tail.compareTo(lexeme);
      if (tailCompareResult == 0) {// 词元与尾部词元排序相同，不放入集合
        return;
      }
      if (tailCompareResult < 0) {// 词元接入链表尾部
        tail.setNext(lexeme);
        lexeme.setPrev(tail);
        tail = lexeme;
        size++;
        return;
      }
      if (head.compareTo(lexeme) > 0) {// 词元接入链表头部
        head.setPrev(lexeme);
        lexeme.setNext(head);
        head = lexeme;
        size++;
        return;
      }
      // 要添加的词元排序在链表头和尾之间，则从尾部上逆，找到小于添加的词元的词元，然后插入该位置
      Lexeme temp = tail;
      while ((temp != null) && (temp.compareTo(lexeme) > 0)) {
        temp = temp.getPrev();
      }
      if ((temp != null) && (temp.compareTo(lexeme) < 0)) {
        lexeme.setPrev(temp);
        lexeme.setNext(temp.getNext());
        temp.getNext().setPrev(lexeme);
        temp.setNext(lexeme);
        size++;
      }
    }

    /**
     * 取出第一个词元（这个词元将被从链表中删除）。
     *
     * @return 第一个词元
     * @author hankai
     * @since Jan 17, 2017 9:55:22 AM
     */
    private Lexeme pollFirst() {
      if (size == 1) {
        final Lexeme first = head;
        head = tail = null;
        size = 0;
        return first;
      } else if (size > 1) {
        final Lexeme first = head;
        head = first.getNext();
        first.setNext(null);
        size--;
        return first;
      }
      return null;
    }

    /**
     * 取出最后一个词元（这个词元将被从链表中删除）。
     *
     * @return 最后一个词元
     * @author hankai
     * @since Jan 17, 2017 9:55:03 AM
     */
    private Lexeme pollLast() {
      if (size == 1) {
        final Lexeme last = head;
        head = tail = null;
        size = 0;
        return last;
      } else if (size > 1) {
        final Lexeme last = tail;
        tail = last.getPrev();
        last.setPrev(null);
        size--;
        return last;
      }
      return null;
    }

    /**
     * 正向遍历并剔除链表中相邻的，且完全包含的词元（进行最大切分的时候，过滤长度较小的交叠词元）。
     *
     * @author hankai
     * @since Jan 17, 2017 9:50:27 AM
     */
    private void excludeOverlap() {
      if (size > 1) {
        Lexeme one = head;
        Lexeme another = one.getNext();
        do {
          if (one.isOverlap(another)) {// 邻近的两个词元完全交叠
            another = another.getNext();
            one.setNext(another);
            if (another != null) {
              another.setPrev(one);
            }
            size--;
          } else {// 词元不完全交叠
            one = another;
            another = another.getNext();
          }
        } while (another != null);
      }
    }

    /**
     * 获取词元个数。
     *
     * @return 词元个数
     * @author hankai
     * @since Jan 17, 2017 9:50:00 AM
     */
    private int size() {
      return size;
    }
  }
}
