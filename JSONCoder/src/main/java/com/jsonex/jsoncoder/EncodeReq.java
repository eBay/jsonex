/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

@Accessors(chain=true)
public class EncodeReq {
  /**
   * If writer is specified, the encoder will only write to the writer and return null. other wise, the encoder will return encoded JSON String
   */
  @Getter @Setter Appendable writer;
  @Getter @Setter Object object;
  /**
   * Optional, if type is not specified, it will use the object.getClass() as the type 
   */
  @Getter @Setter Type type;
  
  private EncodeReq() {}
  
  public static EncodeReq of(Object obj) { return new EncodeReq().setObject(obj); }
}
