
package ren.hankai.cnanalyzer.core;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

/**
 * 地层分词器测试。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:07:30 AM
 */
public class SegmentatorTest {

  @Test
  public void testNext() throws Exception {
    final StringReader sr = new StringReader("日照香炉生紫烟");
    final Segmentator segmentator = new Segmentator(sr);
    Lexeme lexeme = null;
    int i = 0;
    do {
      lexeme = segmentator.next();
      if (lexeme != null) {
        if (i == 0) {
          Assert.assertEquals("日照", lexeme.getText());
        } else if (i == 1) {
          Assert.assertEquals("香炉", lexeme.getText());
        }
      }
      i++;
    } while (lexeme != null);
  }

}
