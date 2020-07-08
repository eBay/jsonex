/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder.coder;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.core.util.ClassUtil;
import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.BeanCoderException;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import static com.jsonex.core.util.StringUtil.toTrimmedStr;

public class CoderArray implements ICoder<Object> {
  public static final InjectableInstance<CoderArray> it = InjectableInstance.of(CoderArray.class);
  public static CoderArray get() { return it.get(); }

  @Override public Class<Object> getType() {return Object.class;}

  @Override
  public TDNode encode(Object obj, Type type, BeanCoderContext ctx, TDNode target) {
    target.setType(TDNode.Type.ARRAY);
    Class<?> cls = ClassUtil.getGenericClass(type);
    for (int i = 0; i < Array.getLength(obj); i++)
      ctx.encode(Array.get(obj, i), cls.getComponentType(), target.createChild());
    return target;
  }

  @Override
  public Object decode(TDNode tdNode, Type type, Object targetObj, BeanCoderContext ctx) {
    if (tdNode.getType() != TDNode.Type.ARRAY)
      throw new BeanCoderException("Incorrect input, the input has to be an array:" + toTrimmedStr(tdNode, 500));
    Class<?> cls = ClassUtil.getGenericClass(type);
    Object result = Array.newInstance(cls.getComponentType(), tdNode.getChildren().size());

    ctx.getNodeToObjectMap().put(tdNode, result);

    for (int i=0; i < tdNode.getChildrenSize(); i++)
      Array.set(result, i, ctx.decode(tdNode.getChild(i), cls.getComponentType(), null, Integer.toString(i)));
    return result;
  }
}