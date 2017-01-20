
package ren.hankai.cnanalyzer.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Lucene 分析器测试。
 * 
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:11:04 AM
 */
public class HkAnalyzerTest {

  @Test
  public void testIndexAndSearch() throws Exception {
    final RAMDirectory directory = new RAMDirectory();
    final HkAnalyzer analyzer = new HkAnalyzer();
    final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    final IndexWriter indexWriter = new IndexWriter(directory, config);
    final Document doc = new Document();
    doc.add(new TextField("text", "我有一头小毛驴我从来也不骑", Store.YES));
    indexWriter.addDocument(doc);
    indexWriter.flush();
    indexWriter.close();

    final IndexReader reader = DirectoryReader.open(directory);
    final IndexSearcher searcher = new IndexSearcher(reader);
    final Query query = HkQueryBuilder.build("text", "毛驴");
    final TopDocs topDocs = searcher.search(query, 1);
    final ScoreDoc[] docs = topDocs.scoreDocs;
    Assert.assertTrue(docs.length == 1);
    final Document result = searcher.doc(docs[0].doc);
    Assert.assertEquals(doc.get("text"), result.get("text"));
  }

}
