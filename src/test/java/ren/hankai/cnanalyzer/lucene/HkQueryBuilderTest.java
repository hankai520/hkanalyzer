
package ren.hankai.cnanalyzer.lucene;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.Test;

/**
 * Lucene 查询构造器测试。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:11:21 AM
 */
public class HkQueryBuilderTest {

  @Test
  public void testBuildStringString() {
    final Query query = HkQueryBuilder.build("text", "关键字");
    System.out.println(query.toString());
    Assert.assertEquals("+text:关键字 +text:关键", query.toString());
  }

  @Test
  public void testBuildStringArrayString() {
    final Query query = HkQueryBuilder.build(new String[] {"text", "content"}, "关键字");
    System.out.println(query.toString());
    Assert.assertEquals("(+text:关键字 +text:关键) (+content:关键字 +content:关键)", query.toString());
  }

  @Test
  public void testBuildStringArrayStringArrayOccurArray() {
    final Query query = HkQueryBuilder.build(new String[] {"text", "content"},
        new String[] {"奥兰多", "海岸线很长"}, new Occur[] {Occur.MUST, Occur.SHOULD});
    System.out.println(query.toString());
    Assert.assertEquals(
        "+(+text:奥兰多 +text:奥兰) (+(+content:海岸线 +(+content:海岸 +content:岸线)) +content:很长)",
        query.toString());
  }

}
