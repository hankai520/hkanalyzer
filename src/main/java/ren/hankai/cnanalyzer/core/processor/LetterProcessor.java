
package ren.hankai.cnanalyzer.core.processor;

import ren.hankai.cnanalyzer.core.SegmentContext;
import ren.hankai.cnanalyzer.core.Lexeme;
import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.util.Arrays;

/**
 * @author hankai
 * @version TODO Missing version number
 * @since Jan 17, 2017 11:29:45 AM
 */
public class LetterProcessor implements WordProcessor {

  // 链接符号
  private static final char[] LETTER_CONNECTORS = new char[] {'#', '&', '+', '-', '.', '@', '_'};
  private static final char[] NUMBER_CONNECTORS = new char[] {',', '.'};
  private int letterStart = -1;
  private int letterEnd = -1;
  private int arabicStart = -1;
  private int arabicEnd = -1;
  private int mixStart = -1;
  private int mixEnd = -1;

  @Override
  public void nextLexeme(SegmentContext context) {
    boolean bufferLockFlag = false;
    bufferLockFlag = processLetters(context) || bufferLockFlag;
    bufferLockFlag = processArabics(context) || bufferLockFlag;
    bufferLockFlag = processMixLetter(context) || bufferLockFlag;
    if (bufferLockFlag) {
      context.lockBuffer(this);
    } else {
      context.unlockBuffer(this);
    }
  }

  private boolean processLetters(SegmentContext context) {
    final int currentCharType = context.getCurrentCharType();
    if (letterStart == -1) {// 当前的分词器尚未开始处理英文字符
      if (CharacterUtil.CHAR_ENGLISH == currentCharType) {
        letterEnd = letterStart = context.getBufferCursor();
      }
    } else {// 当前的分词器正在处理英文字符
      if (CharacterUtil.CHAR_ENGLISH == currentCharType) {
        // 记录当前指针位置为结束位置
        letterEnd = context.getBufferCursor();
      } else {
        // 遇到非字母,输出词元
        final Lexeme newLexeme =
            new Lexeme(context.getBufferOffset(), letterStart, (letterEnd - letterStart) + 1,
                Lexeme.TYPE_LETTER);
        context.addLexeme(newLexeme);
        letterStart = letterEnd = -1;
      }
    }
    if (context.isBufferConsumed()) {
      if ((letterStart != -1) && (letterEnd != -1)) {
        final Lexeme newLexeme =
            new Lexeme(context.getBufferOffset(), letterStart, (letterEnd - letterStart) + 1,
                Lexeme.TYPE_LETTER);
        context.addLexeme(newLexeme);
        letterStart = letterEnd = -1;
      }
    }
    if ((letterStart == -1) && (letterEnd == -1)) {
      return false;
    }
    return true;
  }

  private boolean processArabics(SegmentContext context) {
    final int currentCharType = context.getCurrentCharType();
    if (arabicStart == -1) {
      if (CharacterUtil.CHAR_ARABIC == currentCharType) {
        arabicStart = arabicEnd = context.getBufferCursor();
      }
    } else {
      if (CharacterUtil.CHAR_ARABIC == currentCharType) {
        arabicEnd = context.getBufferCursor();
      } else if ((CharacterUtil.CHAR_OTHERS == currentCharType)
          && isNumConnector(context.getCurrentCharacter())) {
        // 不输出数字，但不标记结束
      } else {
        // 遇到非阿拉伯数字,输出词元
        final Lexeme newLexeme = new Lexeme(context.getBufferOffset(), arabicStart,
            (arabicEnd - arabicStart) + 1, Lexeme.TYPE_NUM);
        context.addLexeme(newLexeme);
        arabicStart = arabicEnd = -1;
      }
    }
    if (context.isBufferConsumed()) {
      if ((arabicStart != -1) && (arabicEnd != -1)) {
        final Lexeme newLexeme = new Lexeme(context.getBufferOffset(), arabicStart,
            (arabicEnd - arabicStart) + 1, Lexeme.TYPE_NUM);
        context.addLexeme(newLexeme);
        arabicStart = arabicEnd = -1;
      }
    }
    if ((arabicStart == -1) && (arabicEnd == -1)) {
      return false;
    }
    return true;
  }

  private boolean processMixLetter(SegmentContext context) {
    final int currentCharType = context.getCurrentCharType();
    if (mixStart == -1) {
      if ((CharacterUtil.CHAR_ARABIC == currentCharType)
          || (CharacterUtil.CHAR_ENGLISH == currentCharType)) {
        mixStart = mixEnd = context.getBufferCursor();
      }
    } else {
      if ((CharacterUtil.CHAR_ARABIC == currentCharType)
          || (CharacterUtil.CHAR_ENGLISH == currentCharType)) {
        mixEnd = context.getBufferCursor();
      } else if ((CharacterUtil.CHAR_OTHERS == currentCharType)
          && isLetterConnector(context.getCurrentCharacter())) {
        mixEnd = context.getBufferCursor();
      } else {
        final Lexeme newLexeme = new Lexeme(context.getBufferOffset(), mixStart,
            (mixEnd - mixStart) + 1, Lexeme.TYPE_LETTER);
        context.addLexeme(newLexeme);
        mixStart = mixEnd = -1;
      }
    }
    if (context.isBufferConsumed()) {
      if ((mixStart != -1) && (mixEnd != -1)) {
        final Lexeme newLexeme = new Lexeme(context.getBufferOffset(), mixStart,
            (mixEnd - mixStart) + 1, Lexeme.TYPE_LETTER);
        context.addLexeme(newLexeme);
        mixStart = mixEnd = -1;
      }
    }
    if ((mixStart == -1) && (mixEnd == -1)) {
      return false;
    } else {
      return true;
    }
  }


  private boolean isLetterConnector(char input) {
    final int index = Arrays.binarySearch(LETTER_CONNECTORS, input);
    return index >= 0;
  }

  private boolean isNumConnector(char input) {
    final int index = Arrays.binarySearch(NUMBER_CONNECTORS, input);
    return index >= 0;
  }

  @Override
  public void reset() {
    letterStart = -1;
    letterEnd = -1;
    letterStart = -1;
    letterEnd = -1;
  }
}
