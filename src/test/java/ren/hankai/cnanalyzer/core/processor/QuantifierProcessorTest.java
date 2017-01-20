
package ren.hankai.cnanalyzer.core.processor;

import org.junit.Assert;
import org.junit.Test;

import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.core.SegmentContext;

/**
 * 中文数词、量词处理器。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:08:37 AM
 */
public class QuantifierProcessorTest {

  @Test
  public void testNextLexeme() {
    final QuantifierProcessor processor = new QuantifierProcessor();
    final char[] buffer = "一个足球场有壹万零捌佰平方千米".toCharArray();
    final SegmentContext context = new SegmentContext(buffer, false);
    Lexeme lexeme = null;
    String text = null;
    int count = 0;
    for (int i = 0; i < buffer.length; i++) {
      context.setBufferCursor(i);
      processor.nextLexeme(context);
      lexeme = context.getFirstLexeme();
      if (lexeme != null) {
        text = String.valueOf(buffer, lexeme.getBegin(), lexeme.getLength());
        if (count == 0) {
          Assert.assertEquals("一", text);
        } else if (count == 1) {
          Assert.assertEquals("壹万零捌佰", text);
        } else if (count == 2) {
          Assert.assertEquals("平方", text);
        } else if (count == 3) {
          Assert.assertEquals("千米", text);
        }
        count++;
      }
    }
    processor.reset();
  }

}
