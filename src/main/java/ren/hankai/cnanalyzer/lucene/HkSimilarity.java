
package ren.hankai.cnanalyzer.lucene;

import org.apache.lucene.search.similarities.BM25Similarity;

/**
 * 自定义的 Lucene 相似度算法组件（Lucene6 默认就是使用的 BM25 算法，此处扩展该类是为了在必要时进行适当优化）。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 2:08:19 PM
 */
public class HkSimilarity extends BM25Similarity {

}
