/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.treedoc;

public class ParseRuntimeException extends RuntimeException {
  final Bookmark bookmark;
  final String digest;

  public ParseRuntimeException(String message, Bookmark bookmark, String digest) {
    super(message);
    this.bookmark = bookmark;
    this.digest = digest;
  }

  public String getMessage() {
    return super.getMessage() + ", " + bookmark + ", digest:" + digest;
  }
}
