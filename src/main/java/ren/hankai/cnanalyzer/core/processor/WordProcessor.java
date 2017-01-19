
package ren.hankai.cnanalyzer.core.processor;

import ren.hankai.cnanalyzer.core.SegmentContext;

/**
 * 文字处理器，作为分词器的支撑组件。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 3:33:04 PM
 */
public interface WordProcessor {

  /**
   * 解析被分析的文本当前位置的下一个词元。
   * 
   * @param context 上下文
   * @author hankai
   * @since Jan 19, 2017 9:52:01 AM
   */
  void nextLexeme(SegmentContext context);

  /**
   * 重置文字处理器状态。
   * 
   * @author hankai
   * @since Jan 19, 2017 9:52:34 AM
   */
  void reset();

}
