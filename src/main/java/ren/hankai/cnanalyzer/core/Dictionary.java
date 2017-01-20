package ren.hankai.cnanalyzer.core;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ren.hankai.cnanalyzer.util.ConfigUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class Dictionary {

  private static final Logger logger = LoggerFactory.getLogger(Dictionary.class);

  private static final String PATH_DIC_MAIN = "/main.dic"; // 主词典
  private static final String PATH_DIC_QUANTIFIER = "/quantifier.dic"; // 中文量词（单位）
  private static final String PATH_DIC_STOP = "/stopword.dic"; // 停止词典

  private static final WordSegment mainWords;
  private static final WordSegment stopWords;// 停止词
  private static final WordSegment quantifiers;// 量词

  static {
    mainWords = loadWords(PATH_DIC_MAIN, null);
    stopWords = loadWords(PATH_DIC_STOP, null);
    quantifiers = loadWords(PATH_DIC_QUANTIFIER, null);

    final String[] userDicts = ConfigUtil.getUserDictionaries();
    if (ArrayUtils.isNotEmpty(userDicts)) {
      for (final String string : userDicts) {
        loadWords(string, mainWords);
      }
    }
    final String[] userStopwords = ConfigUtil.getUserStopwords();
    if (ArrayUtils.isNotEmpty(userStopwords)) {
      for (final String string : userStopwords) {
        loadWords(string, mainWords);
      }
    }
  }

  /**
   * 载入內建词典。
   *
   * @param dictionaryPath 词典类路径
   * @param wordSegment 使用已有的词典存储词条
   * @return 词典树
   * @author hankai
   * @since Jan 18, 2017 4:43:25 PM
   */
  public static WordSegment loadWords(String dictionaryPath, WordSegment wordSegment) {
    WordSegment ws = wordSegment;
    if (ws == null) {
      ws = new WordSegment((char) 0);
    }
    final InputStream is = Dictionary.class.getResourceAsStream(dictionaryPath);
    Objects.requireNonNull(is, "Failed to load main dictionary.");
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
      String word = null;
      do {
        word = reader.readLine();
        if (word != null) {
          word = word.trim();
          if (word.length() > 0) {
            ws.fillSegment(word.toCharArray());
          }
        }
      } while (word != null);
    } catch (final IOException ex) {
      logger.error("Failed to load dictionary: " + dictionaryPath, ex);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (final IOException ex) {
        ex.getMessage();// kindly ignore this exception
      }
    }
    return ws;
  }

  /**
   * 检索匹配主词典
   *
   * @param charArray
   * @param begin
   * @param length
   * @return Hit 匹配结果描述
   */
  public static Hit matchInMainDict(char[] charArray, int begin, int length) {
    return mainWords.match(charArray, begin, length);
  }

  /**
   * 检索匹配主词典, 从已匹配的Hit中直接取出DictSegment，继续向下匹配
   *
   * @param charArray
   * @param currentIndex
   * @param matchedHit
   * @return Hit
   */
  public static Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
    Objects.requireNonNull(matchedHit, "Matched hit cannot be null!");
    final WordSegment ds = matchedHit.getMatchedWord();
    return ds.match(charArray, currentIndex, matchedHit);
  }

  /**
   * 检索匹配量词词典
   *
   * @param charArray
   * @param begin
   * @param length
   * @return Hit 匹配结果描述
   */
  public static Hit matchInQuantifierDict(char[] charArray, int begin, int length) {
    return quantifiers.match(charArray, begin, length);
  }

  /**
   * 判断是否是停止词
   *
   * @param charArray
   * @param begin
   * @param length
   * @return boolean
   */
  public static boolean isStopWord(char[] charArray, int begin, int length) {
    return stopWords.match(charArray, begin, length, null).isMatch();
  }

  /**
   * 批量添加词条到主字典。
   *
   * @param words 词条数组
   * @author hankai
   * @since Jan 19, 2017 9:40:37 AM
   */
  public static void addWord(String... words) {
    if (ArrayUtils.isNotEmpty(words)) {
      for (final String word : words) {
        addWord(word);
      }
    }
  }

  /**
   * 添加词条到主字典。
   *
   * @param word 词条
   * @author hankai
   * @since Jan 19, 2017 9:41:00 AM
   */
  public static void addWord(String word) {
    if ((mainWords != null) && StringUtils.isNotEmpty(word)) {
      mainWords.fillSegment(word.toCharArray());
    }
  }

}
