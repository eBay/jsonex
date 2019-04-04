/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import java.lang.reflect.Type;
import java.math.BigInteger;

import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.ICoder;

public class CoderBigInteger implements ICoder<BigInteger> {
  public Class<BigInteger> getType() {return BigInteger.class;}
  
  @Override public Object encode(BigInteger obj, Type type, BeanCoderContext context) { return obj.toString(); }

  @Override public BigInteger decode(Object obj, Type type, Object targetObj, BeanCoderContext context) { return new BigInteger((String) obj); }
}