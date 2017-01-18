package ren.hankai.cnanalyzer.core;


/**
 * 分词命中信息。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 18, 2017 5:29:18 PM
 */
public class Hit {
  private static final int UNMATCH = 0x00000000; // 不匹配
  private static final int MATCH = 0x00000001; // 匹配
  private static final int PREFIX = 0x00000010; // 前缀匹配

  private int hitState = UNMATCH;

  private WordSegment matchedWord;
  private int begin; // 匹配的词在文本中的起始位置
  private int end; // 匹配的词在文本中的结束位置

  public boolean isMatch() {
    return (hitState & MATCH) > 0;
  }

  public void setMatch() {
    hitState = hitState | MATCH;
  }

  public boolean isPrefix() {
    return (hitState & PREFIX) > 0;
  }

  public void setPrefix() {
    hitState = hitState | PREFIX;
  }

  public boolean isUnmatch() {
    return hitState == UNMATCH;
  }

  public void setUnmatch() {
    hitState = UNMATCH;
  }

  public WordSegment getMatchedWord() {
    return matchedWord;
  }

  public void setMatchedWord(WordSegment matchedWord) {
    this.matchedWord = matchedWord;
  }

  public int getBegin() {
    return begin;
  }

  public void setBegin(int begin) {
    this.begin = begin;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }
}
