package ren.hankai.cnanalyzer.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符集识别辅助工具类
 *
 * @author 林良益
 *
 */
public class CharacterUtil {

  /**
   * 可忽略的字符集。
   */
  public static final int CHAR_OTHERS = 0;
  /**
   * 阿拉伯数字。
   */
  public static final int CHAR_ARABIC = 0X00000001;
  /**
   * 英文字符。
   */
  public static final int CHAR_ENGLISH = 0X00000002;
  /**
   * UTF-8 中文字符集。
   */
  public static final int CHAR_CHINESE = 0X00000004;
  /**
   * 日韩文。
   */
  public static final int CHAR_JK = 0X00000008;

  public static int identifyCharType(char input) {
    if ((input >= '0') && (input <= '9')) {
      return CHAR_ARABIC;
    } else if (((input >= 'a') && (input <= 'z'))
        || ((input >= 'A') && (input <= 'Z'))) {
      return CHAR_ENGLISH;
    } else {
      final Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);
      if ((ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
          || (ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
          || (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
          || (ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)) {
        // 目前已知的中文字符UTF-8集合
        return CHAR_CHINESE;
      } else if ((ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) // 全角数字字符和日韩字符
          // 韩文字符集
          || (ub == Character.UnicodeBlock.HANGUL_SYLLABLES)
          || (ub == Character.UnicodeBlock.HANGUL_JAMO)
          || (ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO)
          // 日文字符集
          || (ub == Character.UnicodeBlock.HIRAGANA) // 平假名
          || (ub == Character.UnicodeBlock.KATAKANA) // 片假名
          || (ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS)) {
        return CHAR_JK;
      }
    }
    return CHAR_OTHERS;
  }

  /**
   * 字符规格化（全角转半角，大写转小写）。
   *
   * @param input 字符
   * @return 规格化字符
   * @author hankai
   * @since Jan 18, 2017 1:10:38 PM
   */
  public static char regularize(char input) {
    if (input == 12288) {
      input = (char) 32;
    } else if ((input > 65280) && (input < 65375)) {
      input = (char) (input - 65248);
    } else if ((input >= 'A') && (input <= 'Z')) {
      input += 32;
    }
    return input;
  }

  /**
   * 是否是间隔符。
   *
   * @param input 字符
   * @return 是否是间隔符
   * @author hankai
   * @since Jan 19, 2017 10:23:21 AM
   */
  public static boolean isSpaceCharacter(char input) {
    return (input == 8) || (input == 9) // 退格、制表符
        || (input == 10) || (input == 13) // 换行
        || (input == 32) || (input == 160); // 空格
  }

  /**
   * 清空字符串中的间隔符。
   *
   * @param string 字符串
   * @return 无间隔符的字符串
   * @author hankai
   * @since Jan 19, 2017 10:51:21 AM
   */
  public static String removeSpeceCharacters(String string) {
    if (StringUtils.isNotEmpty(string)) {
      final StringBuilder builder = new StringBuilder(string.length());
      for (int i = 0; i < string.length(); i++) {
        final char ch = string.charAt(i);
        if (!isSpaceCharacter(ch)) {
          builder.append(ch);
        }
      }
      string = builder.toString();
    }
    return string;
  }

}
