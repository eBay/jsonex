/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.charsource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *  Bookmark used for document parser. It will store the column and line information
 */
@RequiredArgsConstructor @ToString @Getter
public class Bookmark implements Cloneable {
  int line;
  int col;
  int pos;

  public char append(char c) {
    pos++;
    col++;
    if (c == '\n') {
      line++;
      col = 0;
    }
    return c;
  }

  @Override public Bookmark clone() throws CloneNotSupportedException {
    return (Bookmark)super.clone();
  }
}


