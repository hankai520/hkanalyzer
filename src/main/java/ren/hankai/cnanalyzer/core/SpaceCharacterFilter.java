
package ren.hankai.cnanalyzer.core;

import ren.hankai.cnanalyzer.util.CharacterUtil;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 间隔符过滤器，通过此过滤器包装 Reader 来过滤需要忽略的字符。
 *
 * @author hankai
 * @version 1.0.0
 * @since Jan 20, 2017 1:14:11 PM
 */
public class SpaceCharacterFilter extends FilterReader {

  protected SpaceCharacterFilter(Reader in) {
    super(in);
  }

  @Override
  public int read() throws IOException {
    for (int charInt = super.read(); charInt > 0; charInt = super.read()) {
      if (!CharacterUtil.isSpaceCharacter((char) charInt)) {
        return charInt;
      }
    }
    return -1;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    final int count = super.read(cbuf, off, len);
    int spaceCount = 0;
    for (int i = 0; i < count; i++) {
      if (CharacterUtil.isSpaceCharacter(cbuf[i])) {
        spaceCount++;
      } else if (spaceCount > 0) {
        cbuf[i - spaceCount] = cbuf[i];
        cbuf[i] = '\0';
      }
    }
    final int actuallyRead = count - spaceCount;
    return (actuallyRead > 0) ? actuallyRead : -1;
  }

}
