/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder.coder;

import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.BeanCoderException;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;
import com.jsonex.core.util.ClassUtil;
import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import static com.jsonex.core.util.StringUtil.toTrimmedStr;

public class CoderArray implements ICoder<Object> {
  @Getter private static final CoderArray instance = new CoderArray();

  @Override public Class<Object> getType() {return Object.class;}

  @Override
  public TDNode encode(Object obj, Type type, BeanCoderContext ctx, TDNode target) {
    target.setType(TDNode.Type.ARRAY);
    Class<?> cls = ClassUtil.getGenericClass(type);
    for (int i = 0; i < Array.getLength(obj); i++)
      ctx.encode(Array.get(obj, i), cls.getComponentType(), target.createChild(null));
    return target;
  }

  @Override
  public Object decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext ctx) {
    if (jsonNode.getType() != TDNode.Type.ARRAY)
      throw new BeanCoderException("Incorrect input, the input has to be an array:" + toTrimmedStr(jsonNode, 500));
    Class<?> cls = ClassUtil.getGenericClass(type);
    Object array = Array.newInstance(cls.getComponentType(), jsonNode.getChildren().size());
    ctx.getObjectPath().push(array);
    for (int i=0; i < jsonNode.getChildrenSize(); i++)
      Array.set(array, i, ctx.decode(jsonNode.getChild(i), cls.getComponentType(), null, Integer.toString(i)));
    return array;
  }
}