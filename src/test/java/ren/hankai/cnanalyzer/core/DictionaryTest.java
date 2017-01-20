
package ren.hankai.cnanalyzer.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * 词典测试。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 9:06:21 AM
 */
public class DictionaryTest {

  @Test
  public void testLoadWords() {
    final WordSegment segment = Dictionary.loadWords("/test.dic", null);
    Assert.assertNotNull(segment);
    final Hit hit = segment.match("测试词".toCharArray(), 0, 3);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
    Assert.assertTrue(hit.isMatch());
  }

  @Test
  public void testMatchInMainDict() {
    final Hit hit = Dictionary.matchInMainDict("疑是银河落九天".toCharArray(), 0, 7);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
    Assert.assertTrue(hit.isMatch());
  }

  @Test
  public void testMatchWithHit() {
    final Hit hit = Dictionary.matchInMainDict("你好".toCharArray(), 0, 2);
    Assert.assertTrue(hit.isPrefix());
    Assert.assertTrue(hit.isMatch());
    final Hit hit2 = Dictionary.matchWithHit("你好坏".toCharArray(), 2, hit);
    Assert.assertEquals(hit.getMatchedWord(), hit2.getMatchedWord());
    Assert.assertTrue(hit2.isMatch());
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
  }

  @Test
  public void testMatchInQuantifierDict() {
    final Hit hit = Dictionary.matchInQuantifierDict("千克".toCharArray(), 0, 2);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
    Assert.assertTrue(hit.isMatch());
  }

  @Test
  public void testIsStopWord() {
    final boolean isStopword = Dictionary.isStopWord("with".toCharArray(), 0, 4);
    Assert.assertTrue(isStopword);
  }

  @Test
  public void testAddWordStringArray() {
    Hit hit = Dictionary.matchInMainDict("无此词".toCharArray(), 0, 3);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertTrue(hit.isUnmatch());
    Assert.assertFalse(hit.isMatch());
    hit = Dictionary.matchInMainDict("牛风马".toCharArray(), 0, 3);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertTrue(hit.isUnmatch());
    Assert.assertFalse(hit.isMatch());
    Dictionary.addWord("无此词", "牛风马");
    hit = Dictionary.matchInMainDict("无此词".toCharArray(), 0, 3);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
    Assert.assertTrue(hit.isMatch());
    hit = Dictionary.matchInMainDict("牛风马".toCharArray(), 0, 3);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
    Assert.assertTrue(hit.isMatch());
  }

  @Test
  public void testAddWordString() {
    Hit hit = Dictionary.matchInMainDict("问世间情为何物".toCharArray(), 0, 7);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertTrue(hit.isUnmatch());
    Assert.assertFalse(hit.isMatch());
    Dictionary.addWord("问世间情为何物");
    hit = Dictionary.matchInMainDict("问世间情为何物".toCharArray(), 0, 7);
    Assert.assertFalse(hit.isPrefix());
    Assert.assertFalse(hit.isUnmatch());
    Assert.assertTrue(hit.isMatch());
  }

}
