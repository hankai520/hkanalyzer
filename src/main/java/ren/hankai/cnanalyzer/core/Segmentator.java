
package ren.hankai.cnanalyzer.core;

import ren.hankai.cnanalyzer.core.processor.CjkProcessor;
import ren.hankai.cnanalyzer.core.processor.LetterProcessor;
import ren.hankai.cnanalyzer.core.processor.QuantifierProcessor;
import ren.hankai.cnanalyzer.core.processor.WordProcessor;
import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 分词器包装类。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 14, 2017 2:48:12 PM
 */
public class Segmentator {

  private Reader input;
  private final char[] buffer = new char[1024 * 4]; // 4KB
  private final SegmentContext context;
  private final List<WordProcessor> processors = new ArrayList<>();

  public Segmentator(Reader input) {
    this(input, true);
  }

  public Segmentator(Reader input, boolean matchLongerTextOnly) {
    this.input = input;
    context = new SegmentContext(buffer, matchLongerTextOnly);
    processors.add(new QuantifierProcessor());
    processors.add(new CjkProcessor());
    processors.add(new LetterProcessor());
  }

  public synchronized Lexeme next() throws IOException {
    if (context.getResultSize() == 0) {
      final int available = fillBuffer(input);
      if (available <= 0) {
        context.reset();
        return null;
      } else {
        final int analyzedLength = doAnalysis(available);
        resetProcessors();
        // 记录最近一次分析的字符长度
        context.setLastAnalyzedLength(analyzedLength);
        // 记录累计已分析的字符长度
        context.setBufferOffset(context.getBufferOffset() + analyzedLength);
        // 如果使用最大切分，则过滤交叠的短词元
        if (context.isMatchLongerTextOnly()) {
          context.excludeOverlap();
        }
        // 读取词元池中的词元
        return buildLexemeText(context.getFirstLexeme());
      }
    } else {
      // 读取词元池中的已有词元

    }
    return buildLexemeText(context.getFirstLexeme());
  }

  /**
   * 分析缓冲区中的字符。
   *
   * @param available 可供分析的字符数
   * @return 被分析的字符数
   * @author hankai
   * @since Jan 17, 2017 11:00:57 AM
   */
  private int doAnalysis(int available) {
    int analyzedLength = 0;
    for (int buffIndex = 0; buffIndex < available; buffIndex++) {
      context.setBufferCursor(buffIndex);
      buffer[buffIndex] = CharacterUtil.regularize(buffer[buffIndex]);
      for (final WordProcessor processor : processors) {
        processor.nextLexeme(context);
      }
      analyzedLength++;
      if ((available == buffer.length) // 缓冲区已满
          && ((buffIndex < (available - 1)) && (buffIndex > (available - 48))) // 缓冲区即将耗尽
          && !context.isBufferLocked()) { // 没有分词器在占用缓冲区 要中断当前循环（缓冲要进行移位，并再读取数据的操作）
        break;
      }
    }
    return analyzedLength;
  }

  /**
   * 读取字符并填充至缓冲区。
   *
   * @param reader 字符输入流
   * @return 待分析的字符长度
   * @throws IOException IO异常
   * @author hankai
   * @since Jan 17, 2017 10:38:51 AM
   */
  private int fillBuffer(Reader reader) throws IOException {
    int readCount = 0;
    if (context.getBufferOffset() == 0) {
      // 首次读取reader
      readCount = reader.read(buffer);
    } else {
      final int offset = context.getAvailableLength() - context.getLastAnalyzedLength();
      if (offset > 0) {
        // 最近一次读取的大于最近一次处理的字符数，将未处理的字串拷贝到缓冲区头部
        System.arraycopy(buffer, context.getLastAnalyzedLength(), buffer, 0, offset);
        readCount = offset;
      }
      // 继续读取字节，填充缓冲区剩余的部分
      readCount += reader.read(buffer, offset, buffer.length - offset);
    }
    // 记录最后一次从Reader中读入的可用字符长度
    context.setAvailableLength(readCount);
    return readCount;
  }

  /**
   * 用当前缓冲区内容构建词元对应的文本字符串。
   *
   * @param lexeme 词元
   * @return 更新后的词元
   * @author hankai
   * @since Jan 17, 2017 10:40:50 AM
   */
  private Lexeme buildLexemeText(Lexeme lexeme) {
    if (lexeme != null) {
      lexeme.setLexemeText(String.valueOf(buffer, lexeme.getBegin(), lexeme.getLength()));
      return lexeme;
    } else {
      return null;
    }
  }

  /**
   * 重置文字处理器。
   *
   * @author hankai
   * @since Jan 17, 2017 10:45:17 AM
   */
  private void resetProcessors() {
    for (final WordProcessor processor : processors) {
      processor.reset();
    }
  }

  /**
   * 重置分词器到初始状态
   *
   * @param input
   */
  /**
   * 重置分词器状态。
   *
   * @param input 字符输入流
   * @author hankai
   * @since Jan 17, 2017 10:41:44 AM
   */
  public synchronized void reset(Reader input) {
    this.input = input;
    context.reset();
    resetProcessors();
  }

}
