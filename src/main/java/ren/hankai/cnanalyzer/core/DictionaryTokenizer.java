
package ren.hankai.cnanalyzer.core;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义的 Lucene 分词组件。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 2:18:22 PM
 */
public class DictionaryTokenizer extends Tokenizer {

  private final CharTermAttribute termAttribute;
  private final OffsetAttribute offsetAttribute;
  private int endOffset;
  private final Segmentator segmentator;
  private final List<String> words = new ArrayList<>(); // 缓存分词结果

  public DictionaryTokenizer() {
    this(false);
  }

  public DictionaryTokenizer(boolean matchLongerTextOnly) {
    termAttribute = addAttribute(CharTermAttribute.class);
    offsetAttribute = addAttribute(OffsetAttribute.class);
    segmentator = new Segmentator(input, matchLongerTextOnly);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    final Lexeme lexeme = segmentator.next();
    if (lexeme != null) {
      termAttribute.append(lexeme.getText());
      termAttribute.setLength(lexeme.getLength());
      offsetAttribute.setOffset(lexeme.getBeginPosition(), lexeme.getEndPosition());
      endOffset = lexeme.getEndPosition();
      words.add(termAttribute.toString());
      return true;
    }
    return false;
  }

  @Override
  public void reset() throws IOException {
    /*
     * 在 incrementToken 被调用之前，此方法会被调用。需要将所依赖的有状态组件都重置为初始状态。
     */
    super.reset();
    segmentator.reset(input);
  }

  @Override
  public void end() throws IOException {
    /*
     * 当 incrementToken 返回 false 时，最后一个分词被处理完 此时最终位移属性可能和最后一个词元的位移属性不同。 比如：最后一个词元 后面有多个空格，此时使用
     * WhitespaceTokenizer，则最终位移是最后 一个空格的位置，而这和最后一个次元一定不相等，他们之间的差是空格的个数。
     */
    offsetAttribute.setOffset(endOffset, endOffset);
  }

  public String getStringComponent() {
    return termAttribute.toString();
  }

  /**
   * 获取 endOffset 字段的值。
   *
   * @return endOffset 字段值
   */
  public int getEndOffset() {
    return endOffset;
  }

  /**
   * 获取 words 字段的值。
   *
   * @return words 字段值
   */
  public List<String> getWords() {
    return new ArrayList<>(words);
  }

}
