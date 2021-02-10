/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

@SuppressWarnings("serial")
public class BeanCoderException extends RuntimeException {
  public BeanCoderException() { super(); }
  public BeanCoderException(String message) { super(message); }
  public BeanCoderException(String message, Throwable cause) { super(message, cause); } 
  public BeanCoderException(Throwable cause) { super(cause); }
}
