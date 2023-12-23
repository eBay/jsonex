/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.charsource;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class ArrayCharSource extends CharSource {
  final char[] buf;
  final int startIndex;
  final int endIndex;

  public ArrayCharSource(char[] buff) { this(buff, 0, buff.length); }
  public ArrayCharSource(String str) { this(str.toCharArray(), 0, str.length()); }

  @Override public char read() {
    if (isEof(0))
      throw new EOFRuntimeException();
    return bookmark.append(buf[startIndex + bookmark.getPos()]);
  }

  @Override public char peek(int i) {
    if (isEof(i))
      throw new EOFRuntimeException();
    return buf[startIndex + bookmark.getPos() + i];
  }

  @Override public boolean isEof(int i) { return startIndex + bookmark.getPos() + i >= endIndex; }

  @Override public boolean readUntil(StringBuilder target, Predicate<CharSource> predicate, int minLen, int maxLen) {
    int startPos = bookmark.getPos();
    int len = 0;
    boolean matched = false;
    for (; len < maxLen && !(isEof(0)); len++) {
      matched = len >= minLen && predicate.test(this);
      if (matched)
        break;
      read();
    }
    if (target != null)
      target.append(buf, startIndex + startPos, len);
    return matched;
  }
}
