/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.BeanCoderException;
import com.ebay.jsoncoder.ICoder;
import com.ebay.jsoncodercore.util.ClassUtil;
import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.ebay.jsoncodercore.util.StringUtil.toTrimmedStr;

public class CoderArray implements ICoder<Object> {
  @Getter private static final CoderArray instance = new CoderArray();

  public Class<Object> getType() {return Object.class;}

  @Override
  public Object encode(Object obj, Type type, BeanCoderContext ctx) {
    Class<?> cls = ClassUtil.getGenericClass(type);
    List<Object> result = new ArrayList<>();
    for(int i = 0; i< Array.getLength(obj); i++)
      result.add(ctx.encode(Array.get(obj, i), cls.getComponentType()));
    return result;
  }

  @Override
  public Object decode(Object obj, Type type, Object targetObj, BeanCoderContext ctx) {
    if (!(obj instanceof List))
      throw new BeanCoderException("Incorrect input, the input has to be an array:" + toTrimmedStr(obj, 500));
    List<?> list = (List<?>) obj;
    Class<?> cls = ClassUtil.getGenericClass(type);
    Object array = Array.newInstance(cls.getComponentType(), list.size());
    ctx.getObjectPath().push(array);
    for (int i=0; i<list.size(); i++)
      Array.set(array, i, ctx.decode(list.get(i), cls.getComponentType(), null, Integer.toString(i)));
    return array;
  }
}