
package ren.hankai.cnanalyzer.core.processor;

import ren.hankai.cnanalyzer.core.Dictionary;
import ren.hankai.cnanalyzer.core.Hit;
import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.core.SegmentContext;
import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 量词、计数词处理器。
 * 
 * @author hankai
 * @version 1.0.0
 * @since Jan 17, 2017 11:32:05 AM
 */
public class QuantifierProcessor implements WordProcessor {

  private static Set<Character> quantifierCharacters = new HashSet<>();
  static {
    final String cnQuantifiers = "〇一二两三四五六七八九十零壹贰叁肆伍陆柒捌玖拾百千万亿拾佰仟萬億兆卅廿";
    final char[] ca = cnQuantifiers.toCharArray();
    for (final char nChar : ca) {
      quantifierCharacters.add(nChar);
    }
  }

  private int start = -1;
  private int end = -1;
  private final List<Hit> hits = new LinkedList<>();

  @Override
  public void nextLexeme(SegmentContext context) {
    processCNumber(context);
    processCount(context);
    if ((start == -1) && (end == -1) && hits.isEmpty()) {
      context.unlockBuffer(this);
    } else {
      context.lockBuffer(this);
    }
  }

  /**
   * 处理数词。
   *
   * @param context 上下文
   * @author hankai
   * @since Jan 18, 2017 3:23:03 PM
   */
  private void processCNumber(SegmentContext context) {
    final int currentCharType = context.getCurrentCharType();
    if ((start == -1) && (end == -1)) {
      if ((CharacterUtil.CHAR_CHINESE == currentCharType)
          && quantifierCharacters.contains(context.getCurrentCharacter())) {
        start = end = context.getBufferCursor();
      }
    } else {
      if ((CharacterUtil.CHAR_CHINESE == currentCharType)
          && quantifierCharacters.contains(context.getCurrentCharacter())) {
        end = context.getBufferCursor();
      } else {
        outputNumLexeme(context);
        start = end = -1;
      }
    }
    if (context.isBufferConsumed()) {
      if ((start != -1) && (end != -1)) {
        outputNumLexeme(context);
        start = end = -1;
      }
    }
  }

  /**
   * 处理量词。
   *
   * @param context 上下文
   * @author hankai
   * @since Jan 18, 2017 3:23:18 PM
   */
  private void processCount(SegmentContext context) {
    if (!needCountScan(context)) {
      return;
    }
    if (CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()) {
      if (!hits.isEmpty()) {
        final Hit[] tmpArray = hits.toArray(new Hit[hits.size()]);
        for (Hit hit : tmpArray) {
          hit = Dictionary.matchWithHit(context.getBuffer(), context.getBufferCursor(), hit);
          if (hit.isMatch()) {
            final Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(),
                (context.getBufferCursor() - hit.getBegin()) + 1, Lexeme.TYPE_NUMCOUNT);
            context.addLexeme(newLexeme);
            if (!hit.isPrefix()) {
              hits.remove(hit);
            }
          } else if (hit.isUnmatch()) {
            hits.remove(hit);
          }
        }
      }
      // 对当前指针位置的字符进行单字匹配
      final Hit singleCharHit =
          Dictionary.matchInQuantifierDict(context.getBuffer(), context.getBufferCursor(), 1);
      if (singleCharHit.isMatch()) {// 首字成量词词
        final Lexeme newLexeme =
            new Lexeme(context.getBufferOffset(), context.getBufferCursor(), 1,
                Lexeme.TYPE_NUMCOUNT);
        context.addLexeme(newLexeme);
        if (singleCharHit.isPrefix()) {
          hits.add(singleCharHit);
        }
      } else if (singleCharHit.isPrefix()) {// 首字为量词前缀
        hits.add(singleCharHit);
      }
    } else {
      hits.clear();
    }
    if (context.isBufferConsumed()) {
      hits.clear();
    }
  }

  /**
   * 判断是否需要扫描量词
   *
   * @return
   */
  private boolean needCountScan(SegmentContext context) {
    if (((start != -1) && (end != -1)) || !hits.isEmpty()) {
      return true;
    } else {
      final Lexeme lastLexeme = context.peekLastLexeme();
      if (lastLexeme != null) {
        final boolean isNeighbor =
            (lastLexeme.getBegin() + lastLexeme.getLength()) == context.getBufferCursor();
        if ((Lexeme.TYPE_NUM == lastLexeme.getType()) && isNeighbor) {
          return true;
        }
      }
    }
    return false;
  }

  private void outputNumLexeme(SegmentContext context) {
    if ((start > -1) && (end > -1)) {
      final Lexeme newLexeme =
          new Lexeme(context.getBufferOffset(), start, (end - start) + 1, Lexeme.TYPE_NUM);
      context.addLexeme(newLexeme);
    }
  }

  @Override
  public void reset() {
    start = end = -1;
    hits.clear();
  }

}
