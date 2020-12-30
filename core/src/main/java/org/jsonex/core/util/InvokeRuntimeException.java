/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

@SuppressWarnings("serial")
public class InvokeRuntimeException extends RuntimeException {
  public InvokeRuntimeException(String message) {
    super(message);
  }

  public InvokeRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
