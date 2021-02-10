/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.charsource;

import lombok.SneakyThrows;

import java.io.Reader;
import java.util.function.Predicate;

public class ReaderCharSource extends CharSource {
  final Reader reader;
  final char[] buf;

  private int loadPos;
  private boolean fullyLoaded;

  private int backupMark;
  StringBuilder backupTarget;

  public ReaderCharSource(Reader reader, int bufSize) {
    this.reader = reader;
    this.buf = new char[bufSize];
  }

  public ReaderCharSource(Reader reader) { this(reader, 1024); }

  /**
   * @return false indicate EOF encountered, no more data read
   */
  @SneakyThrows
  private boolean fill() {
    if (fullyLoaded)
      return false;

    flushBackupTarget();

    int toLoad = getPos() + buf.length - loadPos;
    while(toLoad > 0) {
      int start = loadPos % buf.length;
      int len = reader.read(buf, start, Math.min(buf.length - start, toLoad));
      if (len < 0) {
        fullyLoaded = true;
        return false;
      }
      toLoad -= len;
      loadPos += len;
      if (len > 0) {  // Return as soon as new data is read without fully loading the buffer to avoid latency
        break;
      }
    }
    return true;
  }

  @Override public char read() {
    if (getPos() == loadPos) {
      fill();
      if (getPos() == loadPos)
        throw new EOFRuntimeException();
    }
    return bookmark.append(buf[getPos() % buf.length]);
  }

  @Override public char peek(int i) {
    if (isEof(i))
      throw new EOFRuntimeException();

    int p = getPos() + i;
    return buf[p % buf.length];
  }

  @Override public boolean isEof(int i) {
    if (i >= buf.length)
      throw new IllegalArgumentException("can't peek ahead more than buffer size characters");

    int p = getPos() + i;
    if (p >= loadPos) {
      fill();
    }
    return p >= loadPos;
  }

  @Override public boolean readUntil(Predicate<CharSource> predicate, StringBuilder target, int minLen, int maxLen) {
    if (target != null) {
      backupTarget = target;
      backupMark = getPos();
    }
    try {
      boolean matched = false;
      for (int len = 0; len < maxLen && !(isEof(0)); len++) {
        matched = len >= minLen && predicate.test(this);
        if (matched)
          break;
        read();
      }
      flushBackupTarget();
      return matched;
    }finally {
      backupTarget = null;
    }
  }

  private void flushBackupTarget() {
    if (getPos() == backupMark || backupTarget == null)
      return;
    int toBackup = getPos() - backupMark;

    int start = backupMark % buf.length;
    if (toBackup < buf.length - start) {
      backupTarget.append(buf, start, toBackup);
    } else {
      backupTarget.append(buf, start, buf.length - start);
      backupTarget.append(buf, 0, start + toBackup - buf.length);
    }
    backupMark = getPos();
  }
}
