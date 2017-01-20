
package ren.hankai.cnanalyzer.core;

import org.junit.Assert;
import org.junit.Test;

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

}
