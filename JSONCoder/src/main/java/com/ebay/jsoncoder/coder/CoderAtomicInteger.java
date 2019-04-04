/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.ICoder;

public class CoderAtomicInteger implements ICoder<AtomicInteger> {
  public Class<AtomicInteger> getType() {return AtomicInteger.class;}
  
  @Override public Object encode(AtomicInteger obj, Type type, BeanCoderContext context) { return obj.get(); }

  @Override public AtomicInteger decode(Object obj, Type type, Object targetObj, BeanCoderContext context) { return new AtomicInteger((int) obj); }
}