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
import com.ebay.jsoncodercore.util.BeanProperty;
import com.ebay.jsoncodercore.util.ClassUtil;
import com.ebay.jsoncodercore.util.StringUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.ebay.jsoncoder.BeanCoder.HASH_KEY;
import static com.ebay.jsoncodercore.util.ClassUtil.isSimpleType;
import static com.ebay.jsoncodercore.util.StringUtil.toTrimmedStr;

@Slf4j
public class CoderObject implements ICoder<Object> {
  @Getter private static final CoderObject instance = new CoderObject();

  private final static String TYPE_KEY = "$type";

  public Class<Object> getType() { return Object.class; }

  @Override
  public Object encode(Object obj, Type type, BeanCoderContext ctx) {
    JSONCoderOption opt = ctx.getOption();

    Class<?> cls = obj.getClass();  // Use the real object;
    if (opt.isIgnoreSubClassFields(cls) && type != null)
      cls = ClassUtil.getGenericClass(type);

    Map<String, Object> result = new LinkedHashMap<>();//NOPMD

    if(opt.isShowType() || cls != obj.getClass())
      result.put(TYPE_KEY, obj.getClass().getName());

    Map<String, BeanProperty> pds = ClassUtil.getProperties(cls);
    for(BeanProperty pd : pds.values()){
      if(!pd.isReadable(opt.isShowPrivateField()))
        continue;

      if(pd.isImmutable(opt.isShowPrivateField()) && opt.isIgnoreReadOnly())
        continue; //Only mutable attribute will be encoded

      if(pd.isTransient())
        continue;

      if(opt.isFieldSkipped(cls, pd.getName()))
        continue;

      //V3DAL will cause Lazy load exception, we have to catch it
      try{
        Type childType = pd.getGenericType();
        Object v = ctx.encode(pd.get(obj), childType);
        if(v != null)
          result.put(pd.getName(), v);
      }catch(Exception e) {
        log.info("warning during encoding", e);
        //ignore this exception
      }
    }
    return result;
  }

  @Override @SneakyThrows
  public Object decode(Object obj, Type type, Object targetObj, BeanCoderContext ctx) {
    Class<?> cls = ClassUtil.getGenericClass(type);

    if (! (obj instanceof Map)) {
      if (cls.isAssignableFrom(obj.getClass()))
        return obj;  // If cls is Object.class, we don't do further decoding
      throw new BeanCoderException("Expect an Map object, but actual type=" + obj.getClass() + ";o=" + toTrimmedStr(obj, 500));
    }

    Map<String, Object> map = (Map<String, Object>) obj;
    Object subType = map.get(TYPE_KEY);
    if (subType instanceof String) {
      try {
        cls = Class.forName((String) subType);
      } catch(ClassNotFoundException e) {
        throw new BeanCoderException("Incorrect $type:" + subType, e);
      }
    }

    if (cls.isAssignableFrom(obj.getClass()))
      return obj;

    Object result = targetObj;
    if (result == null) {
      Constructor cstr = cls.getDeclaredConstructor();
      cstr.setAccessible(true);
      result = cstr.newInstance();
    }

    ctx.getObjectPath().push(result);
    if (map.containsKey(HASH_KEY))
      ctx.getHashToObjectMap().put((String)map.get(HASH_KEY), result);

    Map<String, BeanProperty> pds = ClassUtil.getProperties(cls);

    JSONCoderOption opt = ctx.getOption();
    for(String key : map.keySet()){
      BeanProperty prop = pds.get(key);
      if(prop == null)  //Certain serializer does't follow java bean naming convention, the attribute name is capitalized
        prop = pds.get(StringUtil.lowerFirst(key));

      if(prop == null){
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("No such attribute:" + key + ",class:" + cls);
        continue;
      }

      int mod = prop.getModifier();
      if(Modifier.isStatic(mod) || prop.isTransient()){
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("Field is static or transient:" + key + ",class:" + cls);
        continue;  //None public, or static, transient
      }

      Object childTargetObj = null;

      if(prop.isReadable(true)
          && !isSimpleType(prop.getType())
          && !Enum.class.isAssignableFrom(prop.getType())
          && !Date.class.isAssignableFrom(prop.getType()))
        // TODO: Exclude more non-container and java bean types to prevent side effect when calling getter method
        // One example of the site effect for getter method is calculated value for example: calculate hash code
        try {
          childTargetObj = prop.get(result);
        } catch (Exception e) {
          // ignore if calling get throws exception
        }

      if (childTargetObj == null && prop.isImmutable(true)) {  //In that case, the attribute has to be mutable.
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("Field is not mutable:" + key + ",class:" + cls);
        continue;
      }

      Type childType = prop.getGenericType();
      Object child = ctx.decode(map.get(key), childType, childTargetObj, key);
      if(childTargetObj != child)
        prop.set(result, child);
    }
    return result;
  }

}