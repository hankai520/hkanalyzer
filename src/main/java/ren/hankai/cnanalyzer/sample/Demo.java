
package ren.hankai.cnanalyzer.sample;

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

import ren.hankai.cnanalyzer.core.DictionaryTokenizer;
import ren.hankai.cnanalyzer.lucene.HkAnalyzer;
import ren.hankai.cnanalyzer.lucene.HkQueryParser;

import java.io.StringReader;

/**
 * 示例代码。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 17, 2017 2:08:10 PM
 */
public class Demo {

  private static RAMDirectory directory = new RAMDirectory();

  public static void main(String[] args) throws Exception {
    // demoTokenizeString();
    demoIndexAndSearch();
  }

  public static void indexText(String text) throws Exception {
    final HkAnalyzer analyzer = new HkAnalyzer();
    final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    final IndexWriter indexWriter = new IndexWriter(directory, config);
    final Document doc = new Document();
    doc.add(new TextField("id", "123", Store.YES));
    doc.add(new TextField("text", text, Store.YES));
    indexWriter.addDocument(doc);
    indexWriter.flush();
    indexWriter.close();
  }

  public static void searchText(String keyword) throws Exception {
    final IndexReader reader = DirectoryReader.open(directory);
    final IndexSearcher searcher = new IndexSearcher(reader);
    final Query query = HkQueryParser.parse("text", keyword);
    // final Query query = new MatchAllDocsQuery();
    // final Query query = new TermsQuery(new Term("text", keyword));
    final TopDocs topDocs = searcher.search(query, 5);
    final ScoreDoc[] docs = topDocs.scoreDocs;
    for (final ScoreDoc sd : docs) {
      final Document doc = searcher.doc(sd.doc);
      System.out.println("搜索到内容：" + doc.get("text"));
    }
  }

  public static void demoIndexAndSearch() throws Exception {
    indexText("我有一头小毛驴我从来也不骑，有一天我心血来潮骑着它上街");
    searchText("一头");
  }

  public static void demoTokenizeString() throws Exception {
    final String text = "aa无此词aa这个词条来自自定义词典";
    final DictionaryTokenizer tokenizer = new DictionaryTokenizer();
    final StringReader sr = new StringReader(text);
    tokenizer.setReader(sr);
    tokenizer.reset();
    while (tokenizer.incrementToken()) {
      System.out.println(tokenizer.getStringComponent());
    }
    tokenizer.close();
  }

}
