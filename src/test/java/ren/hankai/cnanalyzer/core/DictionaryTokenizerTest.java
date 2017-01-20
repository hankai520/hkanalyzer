
package ren.hankai.cnanalyzer.core;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Lucene 分词器测试。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:07:04 AM
 */
public class DictionaryTokenizerTest {

  @Test
  public void testIncrementToken() throws Exception {
    final String text = "疑是银河落九天";
    final DictionaryTokenizer tokenizer = new DictionaryTokenizer();
    final StringReader sr = new StringReader(text);
    tokenizer.setReader(sr);
    tokenizer.reset();
    int i = 0;
    while (tokenizer.incrementToken()) {
      if (i == 0) {
        Assert.assertEquals("疑是银河落九天", tokenizer.getStringComponent());
      } else if (i == 1) {
        Assert.assertEquals("疑是", tokenizer.getStringComponent());
      } else if (i == 2) {
        Assert.assertEquals("银河", tokenizer.getStringComponent());
      } else if (i == 3) {
        Assert.assertEquals("落九天", tokenizer.getStringComponent());
      } else if (i == 4) {
        Assert.assertEquals("九天", tokenizer.getStringComponent());
      } else if (i == 5) {
        Assert.assertEquals("九", tokenizer.getStringComponent());
      }
      i++;
    }
    tokenizer.close();
  }

  @Test
  public void testPerformance() throws Exception {
    final long start = System.currentTimeMillis();
    final DictionaryTokenizer tokenizer = new DictionaryTokenizer();
    final InputStream input = this.getClass().getResourceAsStream("/test.txt");
    if (input != null) {
      final InputStreamReader reader = new InputStreamReader(input);
      tokenizer.setReader(reader);
      tokenizer.reset();
      while (tokenizer.incrementToken()) {
      }
      tokenizer.close();
      reader.close();
      final long end = System.currentTimeMillis();
      final double timespan = ((double) end - start) / 1000;
      final int bytes = tokenizer.getEndOffset();
      final double speed = bytes / 1024 / timespan;
      System.out.println("Segment Speed: " + speed + " kb/s");
      Assert.assertTrue(speed >= 45);// >= 45kb/s
    }
  }
}
