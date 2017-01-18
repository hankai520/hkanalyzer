package ren.hankai.cnanalyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {

  private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
  /**
   * 配置文件路径。
   */
  private static final String CONFIG_FILE_NAME = "/hkanalyzer.properties";
  /**
   * 用户配置多个路径时，使用的分隔符。
   */
  private static final String PATH_SEPARATOR = ",";
  /**
   * 用户自定义词典所用参数名。
   */
  public static final String KEY_USER_DICTIONARIES = "user.dictionaries";
  /**
   * 用户自定义停止词所用参数名。
   */
  public static final String KEY_USER_STOPWORDS = "user.stopwords";
  /**
   * 用于自定义词典路径的参数名数组。
   */
  private static final String[] DICTIONARY_KEYS = {KEY_USER_DICTIONARIES, KEY_USER_STOPWORDS};
  /**
   * 用户自定义词典路径。
   */
  private static Map<String, String[]> dictionaries = new HashMap<>();

  static {
    final Properties props = new Properties();
    final InputStream input = ConfigUtil.class.getResourceAsStream(CONFIG_FILE_NAME);
    if (input != null) {
      try {
        props.load(input);
      } catch (final IOException ex) {
        logger.error("Failed to load dictionaries!", ex);
      }
      for (final String string : DICTIONARY_KEYS) {
        final String paths = props.getProperty(string);
        if (paths != null) {
          final String[] pathArray = paths.trim().split(PATH_SEPARATOR);;
          if ((pathArray != null) && (pathArray.length > 0)) {
            dictionaries.put(string, pathArray);
          }
        }
      }
    } else {
      logger.error("HkAnalyzer configuration file not found!");
    }
  }

  /**
   * 获取用户自定义词典。
   *
   * @return 用户词典
   * @author hankai
   * @since Jan 18, 2017 4:27:30 PM
   */
  public static String[] getUserDictionaries() {
    return dictionaries.get(KEY_USER_DICTIONARIES);
  }

  /**
   * 获取用户自定义的停止词。
   *
   * @return 用户停止词
   * @author hankai
   * @since Jan 18, 2017 4:27:47 PM
   */
  public static String[] getUserStopwords() {
    return dictionaries.get(KEY_USER_STOPWORDS);
  }
}
