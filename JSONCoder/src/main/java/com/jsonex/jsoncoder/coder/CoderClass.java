/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder.coder;

import java.lang.reflect.Type;

import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.BeanCoderException;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;

@SuppressWarnings("rawtypes")
public class CoderClass implements ICoder<Class> {
  @Override public Class<Class> getType() {return Class.class;}
  
  @Override public TDNode encode(Class obj, Type type, BeanCoderContext context, TDNode target) {
    return target.setValue(obj.getCanonicalName());
  }

  @Override public Class decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context) {
    try {
      return Class.forName((String) jsonNode.getValue());
    } catch (Exception e) {
      throw new BeanCoderException("Can't load class: " + jsonNode.getValue(), e);
    }
  }
}