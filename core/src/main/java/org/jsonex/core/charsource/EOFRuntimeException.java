/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.charsource;

@SuppressWarnings("serial")
public class EOFRuntimeException extends RuntimeException{
  public EOFRuntimeException(){}
  public EOFRuntimeException(String msg){super(msg);}
}
