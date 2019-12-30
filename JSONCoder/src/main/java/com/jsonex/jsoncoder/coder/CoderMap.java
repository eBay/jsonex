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
import com.jsonex.jsoncoder.BeanCoderException;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.jsoncoder.JSONCoderOption;
import com.jsonex.treedoc.json.TDJSONWriter;
import com.jsonex.treedoc.TDNode;
import com.jsonex.core.util.BeanConvertContext;
import com.jsonex.core.util.ClassUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.jsonex.jsoncoder.BeanCoder.ID_KEY;
import static com.jsonex.core.util.ClassUtil.isSimpleType;
import static com.jsonex.core.util.StringUtil.toTrimmedStr;

public class CoderMap implements ICoder<Map> {
  public static final InjectableInstance<CoderMap> it = InjectableInstance.of(CoderMap.class);
  public static CoderMap get() { return it.get(); }

  @Getter private static final CoderMap instance = new CoderMap();

  @Override public Class<Map> getType() {return Map.class;}

  @Override public TDNode encode(Map obj, Type type, BeanCoderContext ctx, TDNode target) {
    JSONCoderOption opt = ctx.getOption();

    Map<?,?> map = (Map<?,?>)obj;
    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);

    Type childKeyType = null;
    Type childValueType = null;
    if(actualTypeParameters != null){
      childKeyType = actualTypeParameters[0];
      childValueType = actualTypeParameters[1];
    }

    Class<?> childKeyCls = ClassUtil.getGenericClass(childKeyType);
    if (childKeyCls == null)
      childKeyCls = Object.class;
    if(isSimpleType(childKeyCls) || Enum.class.isAssignableFrom(childKeyCls) || childKeyCls == Object.class || opt.isAlwaysMapKeyAsString()) {
      target.setType(TDNode.Type.MAP);
      // Handle it as Map and put the key as String key
      for(Map.Entry<?, ?> entry : map.entrySet()){
        String key = String.valueOf(entry.getKey());
        ctx.encode(entry.getValue(), childValueType, target.createChild(key));
      }
      return target;
    }

    // Handle it as Map, put the Key, Value in a single list
    // Strange and broken implementation of Map.entrySet in class
    // com.jsonex.dsf.dom.AttributeMap
    // it just return null which breaks the interface contract.
    //noinspection ConstantConditions
    if(map.entrySet() != null){
      target.setType(TDNode.Type.ARRAY);
      TDNode child = target.createChild(null);
      for(Map.Entry<?, ?> entry : map.entrySet()){
        Map<String, Object> entryMap = new LinkedHashMap<>(2);//NOPMD
        ctx.encode(entry.getKey(), childKeyType, child.createChild("key"));
        ctx.encode(entry.getValue(), childValueType, child.createChild("value"));
      }
    }

    return target;
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  @Override public Map decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext ctx) {
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
      if (Modifier.isAbstract(modifier)||
          Modifier.isInterface(modifier)) {
        //Use the default implementation HashMap
        result = new LinkedHashMap<>();
      } else
        result = (Map<Object,Object>) cls.newInstance();
    }

    ctx.getObjectPath().push(result);

    switch (jsonNode.getType()) {
      case MAP:
        String hash = (String) jsonNode.getChildValue(ID_KEY);
        if (hash != null)
          ctx.getHashToObjectMap().put(hash, result);

        for (int i = 0; i < jsonNode.getChildrenSize(); i++) {
          TDNode cn = jsonNode.getChild(i);
          Object key = ClassUtil.stringToSimpleObject(cn.getKey(), ClassUtil.getGenericClass(childKeyType), new BeanConvertContext());
          Object value = ctx.decode(cn, childValueType, result.get(key), i + ".value");
          result.put(key, value);
        }
        return result;
      case ARRAY:
        for (int i = 0; i < jsonNode.getChildrenSize(); i++) {
          TDNode cn = jsonNode.getChild(i);
          Object key = ctx.decode(cn.getChild("key"), childKeyType, null, i + ".key");
          Object value = ctx.decode(cn.getChild("value"), childValueType, result.get(key), i + ".value");
          result.put(key, value);
        }
        return result;
    }
    throw new BeanCoderException("Incorrect input, the input for type:" + cls + " has to be an array or map,  got"
        + toTrimmedStr(TDJSONWriter.get().writeAsString(jsonNode), 500));
  }
}