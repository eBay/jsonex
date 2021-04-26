/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.coder;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.BeanConvertContext;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.BeanCoderException;
import org.jsonex.jsoncoder.ICoder;
import org.jsonex.jsoncoder.JSONCoderOption;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.json.TDJSONWriter;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import static org.jsonex.core.util.ClassUtil.isSimpleType;
import static org.jsonex.core.util.StringUtil.toTrimmedStr;

public class CoderMap implements ICoder<Map> {
  public static final InjectableInstance<CoderMap> it = InjectableInstance.of(CoderMap.class);
  public static CoderMap get() { return it.get(); }

  @Getter private static final CoderMap instance = new CoderMap();

  @Override public Class<Map> getType() { return Map.class; }

  @Override public TDNode encode(Map obj, Type type, BeanCoderContext ctx, TDNode target) {
    JSONCoderOption opt = ctx.getOption();

    Map<?,?> map = (Map<?,?>)obj;
    if (opt.isStrictOrder()
        && !(map instanceof SortedMap) && ! (map instanceof LinkedHashMap) && ! (map instanceof EnumMap)) {
      map = new TreeMap<>(map);  // Due to instability of map iterator, we copy it to TreeMap to make it in stable order.
    }

    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);

    Type childKeyType = null;
    Type childValueType = null;
    if (actualTypeParameters != null) {
      childKeyType = actualTypeParameters[0];
      childValueType = actualTypeParameters[1];
    }

    Class<?> childKeyCls = ClassUtil.getGenericClass(childKeyType);
    if (childKeyCls == null)
      childKeyCls = Object.class;
    if (isSimpleType(childKeyCls) || Enum.class.isAssignableFrom(childKeyCls) || childKeyCls == Object.class
        || opt.isAlwaysMapKeyAsString()) {
      target.setType(TDNode.Type.MAP);
      // Handle it as Map and put the key as String key
      int i = 0;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String key = String.valueOf(entry.getKey());
        ctx.encode(entry.getValue(), childValueType, target.createChild(key));
        if (i++ > ctx.getOption().getMaxElementsPerNode())
          break;
      }
      return target;
    }

    // Handle it as Map, put the Key, Value in a single list
    // Some Map.entrySet() has broken implemenation that returns null
    // it just return null which breaks the interface contract.
    if (map.entrySet() != null) {
      target.setType(TDNode.Type.ARRAY);
      TDNode child = target.createChild(null);
      int i = 0;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        Map<String, Object> entryMap = new LinkedHashMap<>(2);//NOPMD
        ctx.encode(entry.getKey(), childKeyType, child.createChild("key"));
        ctx.encode(entry.getValue(), childValueType, child.createChild("value"));
        if (i++ > ctx.getOption().getMaxElementsPerNode())
          break;
      }
    }

    return target;
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  @Override public Map decode(TDNode tdNode, Type type, Object targetObj, BeanCoderContext ctx) {
    Class<?> cls = ClassUtil.getGenericClass(type);
    // Get the key/value types
    // TODO: call ClassUtil.getGenericTypeActualParamsForInterface() instead to support MultiValueMap
    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);
    if(actualTypeParameters == null)
      throw new BeanCoderException("BeanCoder: For Map type, you have to specify the actual key, value type: " + cls);
    Type childKeyType = actualTypeParameters[0];
    Type childValueType = actualTypeParameters[1];

    //Instantiate the object
    Map<Object,Object> result = (Map<Object,Object>)targetObj;
    if (result == null) {
      int modifier = cls.getModifiers();
      if (Modifier.isAbstract(modifier) || Modifier.isInterface(modifier)) {
        //Use the default implementation HashMap
        result = new LinkedHashMap<>();
      } else
        result = (Map<Object,Object>) cls.newInstance();
    }

    ctx.getNodeToObjectMap().put(tdNode, result);

    switch (tdNode.getType()) {
      case MAP:
        for (int i = 0; i < tdNode.getChildrenSize(); i++) {
          TDNode cn = tdNode.getChild(i);
          Object key = ClassUtil.toSimpleObject(cn.getKey(), ClassUtil.getGenericClass(childKeyType), new BeanConvertContext());
          Object value = ctx.decode(cn, childValueType, result.get(key), i + ".value");
          result.put(key, value);
        }
        return result;
      case ARRAY:
        for (int i = 0; i < tdNode.getChildrenSize(); i++) {
          TDNode cn = tdNode.getChild(i);
          Object key = ctx.decode(cn.getChild("key"), childKeyType, null, i + ".key");
          Object value = ctx.decode(cn.getChild("value"), childValueType, result.get(key), i + ".value");
          result.put(key, value);
        }
        return result;
    }
    throw new BeanCoderException("Incorrect input, the input for type:" + cls + " has to be an array or map, got"
        + toTrimmedStr(TDJSONWriter.get().writeAsString(tdNode), 500));
  }
}