
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
import ren.hankai.cnanalyzer.lucene.HkQueryBuilder;
import ren.hankai.cnanalyzer.lucene.HkSimilarity;

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
    demoTokenizeString();
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
    searcher.setSimilarity(new HkSimilarity());
    // HkQueryParser.isLongerTextOnly = true; //使用最大且分词
    final Query query = HkQueryBuilder.build("text", keyword);
    // final Query query = new MatchAllDocsQuery();
    // final Query query = new TermsQuery(new Term("text", keyword));
    final TopDocs topDocs = searcher.search(query, 5);
    final ScoreDoc[] docs = topDocs.scoreDocs;
    System.out.println("\n\n搜索到内容:");
    for (final ScoreDoc sd : docs) {
      final Document doc = searcher.doc(sd.doc);
      System.out.println(doc.get("text"));
    }
  }

  public static void demoIndexAndSearch() throws Exception {
    indexText("唐朝诗人李白曾写过一首诗《望庐山瀑布》，日照香炉生紫烟，遥看瀑布挂前川，飞流直下三千尺，疑是银河落九天");
    // 使用 queryparser 可以清除间隔符
    // searchText("小 毛 \n \t 驴");
    searchText("唐朝银河");
  }

  public static void demoTokenizeString() throws Exception {
    final String text = "唐朝诗人李白曾写过一首诗《望庐山瀑布》，日照香炉生紫烟，遥看瀑布挂前川，飞流直下三千尺，疑是银河落九天";
    // final String text = "发票的票额是壹万叁仟陆百〇玖公顷";
    // final String text = "aa无此词aa这个词条来自自定义词典";
    final DictionaryTokenizer tokenizer = new DictionaryTokenizer();
    final StringReader sr = new StringReader(text);
    tokenizer.setReader(sr);
    tokenizer.reset();
    System.out.println("\n\n分词结果:");
    while (tokenizer.incrementToken()) {
      System.out.println(tokenizer.getStringComponent());
    }
    tokenizer.close();
  }

}
