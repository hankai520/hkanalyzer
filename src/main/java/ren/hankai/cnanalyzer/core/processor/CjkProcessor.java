
package ren.hankai.cnanalyzer.core.processor;

import ren.hankai.cnanalyzer.core.Dictionary;
import ren.hankai.cnanalyzer.core.Hit;
import ren.hankai.cnanalyzer.core.SegmentContext;
import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * 中文（CJK）词元处理子分词器，涵盖一下范围 1.中文词语 2.姓名 3.地名 4.未知词（单字切分） 5.日文/韩文（单字切分）
 *
 * @author 林良益
 * @version 3.2
 */
public class CjkProcessor implements WordProcessor {

  private final List<Hit> hits = new LinkedList<>();

  @Override
  public void nextLexeme(SegmentContext context) {
    if (CharacterUtil.CHAR_OTHERS != context.getCurrentCharType()) {
      if (!hits.isEmpty()) {
        final Hit[] tmpArray = hits.toArray(new Hit[hits.size()]);
        for (Hit hit : tmpArray) {
          hit = Dictionary.matchWithHit(context.getBuffer(), context.getBufferCursor(), hit);
          if (hit.isMatch()) {
            final Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(),
                (context.getBufferCursor() - hit.getBegin()) + 1, Lexeme.TYPE_CJK_NORMAL);
            context.addLexeme(newLexeme);
            if (!hit.isPrefix()) { // 不是词前缀，hit不需要继续匹配，移除
              hits.remove(hit);
            }
          } else if (hit.isUnmatch()) { // hit不是词，移除
            hits.remove(hit);
          }
        }
      }
      final Hit singleCharHit =
          Dictionary.matchInMainDict(context.getBuffer(), context.getBufferCursor(), 1);
      if (singleCharHit.isMatch()) {// 首字成词
        final Lexeme newLexeme =
            new Lexeme(context.getBufferOffset(), context.getBufferCursor(), 1,
                Lexeme.TYPE_CJK_NORMAL);
        context.addLexeme(newLexeme);
        if (singleCharHit.isPrefix()) { // 同时也是词前缀
          hits.add(singleCharHit);
        }
      } else if (singleCharHit.isPrefix()) {// 首字为词前缀
        hits.add(singleCharHit);
      }
    } else {
      hits.clear();
    }

    // 判断缓冲区是否已经读完
    if (context.isBufferConsumed()) {
      // 清空队列
      hits.clear();
    }
    // 判断是否锁定缓冲区
    if (hits.size() == 0) {
      context.unlockBuffer(this);
    } else {
      context.lockBuffer(this);
    }
  }

  @Override
  public void reset() {
    hits.clear();
  }
}
