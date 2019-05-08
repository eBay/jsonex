/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder.coder;

import java.lang.reflect.Type;
import java.math.BigInteger;

import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;

public class CoderBigInteger implements ICoder<BigInteger> {
  @Override public Class<BigInteger> getType() {return BigInteger.class;}
  
  @Override public TDNode encode(BigInteger obj, Type type, BeanCoderContext context, TDNode target) { return target.setValue(obj.toString()); }

  @Override public BigInteger decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context) {
    return new BigInteger((String) jsonNode.getValue());
  }
}