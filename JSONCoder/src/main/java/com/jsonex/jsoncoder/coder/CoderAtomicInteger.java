/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder.coder;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

public class CoderAtomicInteger implements ICoder<AtomicInteger> {
  public static final InjectableInstance<CoderAtomicInteger> it = InjectableInstance.of(CoderAtomicInteger.class);
  public static CoderAtomicInteger get() { return it.get(); }

  @Override public Class<AtomicInteger> getType() {return AtomicInteger.class;}
  
  @Override public TDNode encode(AtomicInteger obj, Type type, BeanCoderContext context, TDNode target) { return target.setValue(obj.get()); }

  @Override public AtomicInteger decode(TDNode tdNode, Type type, Object targetObj, BeanCoderContext context) {
    return new AtomicInteger((int)tdNode.getValue());
  }
}