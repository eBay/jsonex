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
import com.ebay.jsoncoder.JSONCoderOption;
import com.ebay.jsoncodercore.util.ClassUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ebay.jsoncoder.BeanCoder.HASH_KEY;
import static com.ebay.jsoncodercore.util.ClassUtil.isSimpleType;
import static com.ebay.jsoncodercore.util.StringUtil.toTrimmedStr;

public class CoderMap implements ICoder<Map> {
  @Getter private static final CoderMap instance = new CoderMap();

  public Class<Map> getType() {return Map.class;}

  @Override
  public Object encode(Map obj, Type type, BeanCoderContext ctx) {
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
      // Handle it as Map and put the key as String key
      Map<String, Object> result = new LinkedHashMap<>();//NOPMD
      for(Map.Entry<?, ?> entry : map.entrySet()){
        String key = String.valueOf(entry.getKey());
        if(Enum.class.isAssignableFrom(childKeyCls))
          key = String.valueOf(ctx.encode(entry.getKey(), childKeyType));
        result.put(key, ctx.encode(entry.getValue(), childValueType));
      }
      return result;
    }

    // Handle it as Map, put the Key, Value in a single list
    // Strange and broken implementation of Map.entrySet in class
    // com.ebay.dsf.dom.AttributeMap
    // it just return null which breaks the interface contract.
    List<Object> result = new ArrayList<>();
    //noinspection ConstantConditions
    if(map.entrySet() != null){
      for(Map.Entry<?, ?> entry : map.entrySet()){
        Map<String, Object> entryMap = new LinkedHashMap<>(2);//NOPMD
        entryMap.put("key", ctx.encode(entry.getKey(), childKeyType));
        entryMap.put("value", ctx.encode(entry.getValue(), childValueType));
        result.add(entryMap);
      }
    }

    return result;
  }

  @Override @SneakyThrows
  public Map decode(Object obj, Type type, Object targetObj, BeanCoderContext ctx) {
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
        result = new LinkedHashMap();
      } else
        result = (Map<Object,Object>) cls.newInstance();
    }

    ctx.getObjectPath().push(result);

    if (obj instanceof Map) {
      int i = 0;//NOPMD
      Map<?, ?> map = (Map<?, ?>) obj;

      if (map.containsKey(HASH_KEY)) {
        ctx.getHashToObjectMap().put((String) map.get(HASH_KEY), result);
        map.remove(HASH_KEY);
      }

      for (Map.Entry<?, ?> entry : map.entrySet()) {
        Object key = ctx.decode(entry.getKey(), childKeyType, null, i + ".key");
        Object value = ctx.decode(entry.getValue(), childValueType, result.get(key), i + ".val");
        result.put(key, value);
        i++;
      }
      return result;
    } else if (obj instanceof List) {
      List<?> list = (List<?>) obj;
      int i = 0;
      for(Map<String, Object> entryMap : (List<Map<String, Object>>)list){
        Object key = ctx.decode(entryMap.get("key"), childKeyType, null, i + ".key");
        Object value = ctx.decode(entryMap.get("value"), childValueType, result.get(key), i + ".val");
        result.put(key, value);
        i++;
      }
      return result;
    }
    throw new BeanCoderException("Incorrect input, the input for type:" + cls + " has to be an array or map,  got" + toTrimmedStr(obj, 500));
  }
}