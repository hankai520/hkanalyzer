
package ren.hankai.cnanalyzer.core.processor;

import ren.hankai.cnanalyzer.core.SegmentContext;

/**
 * @author hankai
 * @version TODO Missing version number
 * @since Jan 14, 2017 3:33:04 PM
 */
public interface WordProcessor {

  void nextLexeme(SegmentContext context);

  void reset();

}
