
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

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    final DictionaryTokenizer tokenizer = new DictionaryTokenizer();
    return new TokenStreamComponents(tokenizer);
  }

}
