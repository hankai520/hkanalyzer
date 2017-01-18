
package ren.hankai.cnanalyzer.core;

/**
 * 词元模型，存储分词结果。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 4:39:58 PM
 */
public final class Lexeme implements Comparable<Lexeme> {

  public static final int TYPE_CJK_NORMAL = 0; // 普通词元
  public static final int TYPE_NUM = 10; // 数词
  public static final int TYPE_NUMCOUNT = 11; // 量词
  public static final int TYPE_LETTER = 20; // 英文

  // 词元的起始位移
  private int offset;
  // 词元的相对起始位置
  private int begin;
  // 词元的长度
  private int length;
  // 词元文本
  private String text;
  // 词元类型
  private int type;
  // 当前词元的前一个词元
  private Lexeme prev;
  // 当前词元的后一个词元
  private Lexeme next;

  public Lexeme(int offset, int begin, int length, int type) {
    this.offset = offset;
    this.begin = begin;
    if (length < 0) {
      throw new IllegalArgumentException("length < 0");
    }
    this.length = length;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if ((o != null) && (o instanceof Lexeme)) {
      final Lexeme other = (Lexeme) o;
      if ((offset == other.getOffset())
          && (begin == other.getBegin())
          && (length == other.getLength())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int absBegin = getBeginPosition();
    final int absEnd = getEndPosition();
    return (absBegin * 37) + (absEnd * 31) + (((absBegin * absEnd) % getLength()) * 11);
  }

  @Override
  public int compareTo(Lexeme other) {
    if (begin < other.getBegin()) {
      return -1;
    } else if (begin == other.getBegin()) {
      if (length > other.getLength()) {
        return -1;
      } else if (length == other.getLength()) {
        return 0;
      } else {
        return 1;
      }
    } else {
      return 1;
    }
  }

  /**
   * 判断词元是否彼此完全包含（两种情景：1， other 完全包含当前实例，2，当前实例完全包含 other）。
   *
   * @param other 目标词元
   * @return 是否包含
   * @author hankai
   * @since Jan 18, 2017 5:18:31 PM
   */
  public boolean isOverlap(Lexeme other) {
    if (other != null) {
      if ((getBeginPosition() <= other.getBeginPosition())
          && (getEndPosition() >= other.getEndPosition())) {
        return true;
      } else if ((getBeginPosition() >= other.getBeginPosition())
          && (getEndPosition() <= other.getEndPosition())) {
        return true;
      }
    }
    return false;
  }

  /**
   * 获取词元在文本中的起始位置。
   *
   * @return 起始位置
   * @author hankai
   * @since Jan 18, 2017 5:21:04 PM
   */
  public int getBeginPosition() {
    return offset + begin;
  }

  /**
   * 获取词元在文本中的结束位置。
   *
   * @return 结束位置
   * @author hankai
   * @since Jan 18, 2017 5:21:15 PM
   */
  public int getEndPosition() {
    return offset + begin + length;
  }

  /**
   * 获取词元文本。
   *
   * @return 词元文本
   * @author hankai
   * @since Jan 18, 2017 5:23:30 PM
   */
  public String getText() {
    return text == null ? "" : text;
  }

  /**
   * 设置词元文本。
   *
   * @param text 文本
   * @author hankai
   * @since Jan 18, 2017 5:23:57 PM
   */
  public void setLexemeText(String text) {
    if (text == null) {
      this.text = "";
      length = 0;
    } else {
      this.text = text;
      length = text.length();
    }
  }

  /**
   * 获取 offset 字段的值。
   *
   * @return offset 字段值
   */
  public int getOffset() {
    return offset;
  }

  /**
   * 设置 offset 字段的值。
   *
   * @param offset offset 字段的值
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * 获取 begin 字段的值。
   *
   * @return begin 字段值
   */
  public int getBegin() {
    return begin;
  }

  /**
   * 设置 begin 字段的值。
   *
   * @param begin begin 字段的值
   */
  public void setBegin(int begin) {
    this.begin = begin;
  }

  /**
   * 获取 length 字段的值。
   *
   * @return length 字段值
   */
  public int getLength() {
    return length;
  }

  /**
   * 获取 type 字段的值。
   *
   * @return type 字段值
   */
  public int getType() {
    return type;
  }

  /**
   * 设置 type 字段的值。
   *
   * @param type type 字段的值
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * 获取 prev 字段的值。
   *
   * @return prev 字段值
   */
  public Lexeme getPrev() {
    return prev;
  }

  /**
   * 设置 prev 字段的值。
   *
   * @param prev prev 字段的值
   */
  public void setPrev(Lexeme prev) {
    this.prev = prev;
  }

  /**
   * 获取 next 字段的值。
   *
   * @return next 字段值
   */
  public Lexeme getNext() {
    return next;
  }

  /**
   * 设置 next 字段的值。
   *
   * @param next next 字段的值
   */
  public void setNext(Lexeme next) {
    this.next = next;
  }

}
