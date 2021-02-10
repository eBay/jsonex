/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.coder;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.ICoder;
import org.jsonex.treedoc.TDNode;

import java.lang.reflect.Type;
import java.math.BigInteger;

public class CoderBigInteger implements ICoder<BigInteger> {
  public static final InjectableInstance<CoderBigInteger> it = InjectableInstance.of(CoderBigInteger.class);
  public static CoderBigInteger get() { return it.get(); }

  @Override public Class<BigInteger> getType() {return BigInteger.class;}
  
  @Override public TDNode encode(BigInteger obj, Type type, BeanCoderContext context, TDNode target) { return target.setValue(obj.toString()); }

  @Override public BigInteger decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context) {
    return new BigInteger((String) jsonNode.getValue());
  }
}