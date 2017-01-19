
package ren.hankai.cnanalyzer.lucene;

import org.apache.lucene.analysis.Analyzer;

import ren.hankai.cnanalyzer.core.DictionaryTokenizer;

/**
 * 自定义 Lucene 分析器组件。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 2:04:46 PM
 */
public class HkAnalyzer extends Analyzer {

  private final DictionaryTokenizer tokenizer;

  public HkAnalyzer() {
    this(false);
  }

  public HkAnalyzer(boolean matchLongerText) {
    tokenizer = new DictionaryTokenizer(matchLongerText);
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    return new TokenStreamComponents(tokenizer);
  }

}
