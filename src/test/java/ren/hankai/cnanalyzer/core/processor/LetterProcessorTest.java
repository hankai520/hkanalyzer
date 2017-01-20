
package ren.hankai.cnanalyzer.core.processor;

import org.junit.Assert;
import org.junit.Test;

import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.core.SegmentContext;

/**
 * 字母处理器。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:08:23 AM
 */
public class LetterProcessorTest {

  @Test
  public void testNextLexeme() {
    final LetterProcessor processor = new LetterProcessor();
    final char[] buffer = "windows2000 is shit".toCharArray();
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
          Assert.assertEquals("windows", text);
        } else if (count == 1) {
          Assert.assertEquals("windows2000", text);
        } else if (count == 2) {
          Assert.assertEquals("2000", text);
        }
        count++;
      }
    }
    processor.reset();
  }

}
