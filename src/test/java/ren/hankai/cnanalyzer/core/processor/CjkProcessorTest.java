
package ren.hankai.cnanalyzer.core.processor;

import org.junit.Assert;
import org.junit.Test;

import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.core.SegmentContext;

/**
 * 中日韩文分词器。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:07:59 AM
 */
public class CjkProcessorTest {

  @Test
  public void testNextLexeme() {
    final CjkProcessor processor = new CjkProcessor();
    final char[] buffer = "我有一头小毛驴我从来也不骑".toCharArray();
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
          Assert.assertEquals("一头", text);
        } else if (count == 1) {
          Assert.assertEquals("小毛", text);
        } else if (count == 2) {
          Assert.assertEquals("小毛驴", text);
        } else if (count == 3) {
          Assert.assertEquals("毛驴", text);
        } else if (count == 4) {
          Assert.assertEquals("从来", text);
        } else if (count == 5) {
          Assert.assertEquals("不骑", text);
        }
        count++;
      }
    }
    processor.reset();
  }

}
