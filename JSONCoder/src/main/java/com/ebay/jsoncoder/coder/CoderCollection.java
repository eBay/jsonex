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
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ebay.jsoncodercore.util.StringUtil.toTrimmedStr;

public class CoderCollection implements ICoder<Collection> {
  @Getter private static final CoderCollection instance = new CoderCollection();

  public Class<Collection> getType() {return Collection.class;}

  @Override
  public Object encode(Collection obj, Type type, BeanCoderContext ctx) {
    List<Object> result = new ArrayList<>();

    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);
    Type childType = null;
    if(actualTypeParameters != null)
      childType = actualTypeParameters[0];

    for(Object o1 : (Collection<?>)obj)
      result.add(ctx.encode(o1, childType));

    return result;
  }

  @Override @SneakyThrows
  public Collection decode(Object obj, Type type, Object targetObj, BeanCoderContext ctx) {
    if (!(obj instanceof List))
      throw new BeanCoderException("Incorrect input, the input has to be an array:" + toTrimmedStr(obj, 500));

    Class<?> cls = ClassUtil.getGenericClass(type);

    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);
    if (actualTypeParameters == null)
      throw new BeanCoderException("For collection type, you have to specify the actual type: " + cls);
    Type childType = actualTypeParameters[0];

    Collection<Object> result = (Collection<Object>) targetObj;
    if (result == null) {
      int modifier = cls.getModifiers();
      if (Modifier.isAbstract(modifier) ||
          Modifier.isInterface(modifier)) {
        //Use the default implementation ArrayList
        if (EnumSet.class.isAssignableFrom(cls))
          result = (EnumSet) EnumSet.noneOf((Class<Enum>) childType);
        else if (Set.class.isAssignableFrom(cls))
          result = new HashSet<>();
        else
          result = new ArrayList<>();
      } else
        result = (Collection<Object>) cls.newInstance();
    }

    List<?> list = (List<?>) obj;
    ctx.getObjectPath().push(result);
    for (int i = 0; i < list.size(); i++)
      result.add(ctx.decode(list.get(i), childType, null, Integer.toString(i)));
    return result;
  }
}