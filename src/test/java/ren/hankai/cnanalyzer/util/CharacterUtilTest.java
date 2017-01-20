
package ren.hankai.cnanalyzer.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * 字符工具类测试。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:11:47 AM
 */
public class CharacterUtilTest {

  @Test
  public void testIdentifyCharType() {
    final int type = CharacterUtil.identifyCharType('中');
    Assert.assertTrue(CharacterUtil.CHAR_CHINESE == type);
  }

  @Test
  public void testRegularize() {
    char ch = CharacterUtil.regularize('C');
    Assert.assertEquals('c', ch);
    ch = CharacterUtil.regularize('　');// 全角空格
    Assert.assertEquals(' ', ch);// 半角空格
  }

  @Test
  public void testIsSpaceCharacter() {
    Assert.assertTrue(CharacterUtil.isSpaceCharacter(' '));
    Assert.assertTrue(CharacterUtil.isSpaceCharacter('\n'));
    Assert.assertTrue(CharacterUtil.isSpaceCharacter('\t'));
    Assert.assertTrue(CharacterUtil.isSpaceCharacter('\b'));

    Assert.assertFalse(CharacterUtil.isSpaceCharacter('a'));
  }

  @Test
  public void testRemoveSpeceCharacters() {
    final String string = CharacterUtil.removeSpeceCharacters("我 是\t中\n 国\b 人");
    Assert.assertEquals("我是中国人", string);
  }

}
