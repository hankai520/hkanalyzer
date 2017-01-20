
package ren.hankai.cnanalyzer.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * 配置类测试。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:12:19 AM
 */
public class ConfigUtilTest {

  @Test
  public void testGetUserDictionaries() {
    final String[] userDicts = ConfigUtil.getUserDictionaries();
    Assert.assertNotNull(userDicts);
    Assert.assertTrue(userDicts.length == 1);
    Assert.assertEquals("/my_dict.dic", userDicts[0]);
  }

  @Test
  public void testGetUserStopwords() {
    final String[] userStopwords = ConfigUtil.getUserStopwords();
    Assert.assertNotNull(userStopwords);
    Assert.assertTrue(userStopwords.length == 1);
    Assert.assertEquals("/my_stopwords.dic", userStopwords[0]);
  }

}
