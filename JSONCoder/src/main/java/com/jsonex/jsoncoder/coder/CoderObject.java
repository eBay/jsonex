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
import com.jsonex.jsoncoder.JSONCoderOption;
import com.jsonex.treedoc.TDJSONWriter;
import com.jsonex.treedoc.TDNode;
import com.jsonex.core.util.BeanProperty;
import com.jsonex.core.util.ClassUtil;
import com.jsonex.core.util.ListUtil;
import com.jsonex.core.util.StringUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

import static com.jsonex.jsoncoder.BeanCoder.HASH_KEY;
import static com.jsonex.core.util.ClassUtil.isSimpleType;
import static com.jsonex.core.util.StringUtil.toTrimmedStr;

@Slf4j
public class CoderObject implements ICoder<Object> {
  @Getter private static final CoderObject instance = new CoderObject();

  private final static String TYPE_KEY = "$type";

  @Override public Class<Object> getType() { return Object.class; }

  @Override public TDNode encode(Object obj, Type type, BeanCoderContext ctx, TDNode target) {
    target.setType(TDNode.Type.MAP);
    JSONCoderOption opt = ctx.getOption();

    Class<?> cls = obj.getClass();  // Use the real object;
    if (opt.isIgnoreSubClassFields(cls) && type != null)
      cls = ClassUtil.getGenericClass(type);

    if(opt.isShowType() || cls != obj.getClass())
      target.createChild(TYPE_KEY).setValue(obj.getClass().getName());

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
        Object co = pd.get(obj);
        if (co != null) {
          TDNode cn = ctx.encode(co, childType, target.createChild(pd.getName()));
          if (cn.getType() == TDNode.Type.SIMPLE && cn.getValue() == null)
            ListUtil.removeLast(target.getChildren());
        }
      }catch(Exception e) {
        log.info("warning during encoding", e);
        //ignore this exception
      }
    }
    return target;
  }

  @Override @SneakyThrows
  public Object decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext ctx) {
    Class<?> cls = ClassUtil.getGenericClass(type);

    if (jsonNode.getType() != TDNode.Type.MAP) {
      if (jsonNode.getValue() != null && cls.isAssignableFrom(jsonNode.getValue().getClass()))
        return jsonNode.getValue();  // SIMPLE type
      if (cls.isAssignableFrom(TDNode.class))
        return jsonNode;  // If cls is Object.class, we don't do further decoding
      throw new BeanCoderException("Expect an Map object, but actual type=" + jsonNode.getType() +
          ";o=" + toTrimmedStr(TDJSONWriter.getInstance().writeAsString(jsonNode) + ": pos=" + jsonNode.getStart(), 500));
    }
    Object subType = jsonNode.getChildValue(TYPE_KEY);
    if (subType instanceof String) {
      try {
        cls = Class.forName((String) subType);
      } catch(ClassNotFoundException e) {
        throw new BeanCoderException("Incorrect $type:" + subType, e);
      }
    }

    // TODO: confirm the test case
    if (cls.isAssignableFrom(TDNode.class))
      return jsonNode;

    Object result = targetObj;
    if (result == null) {
      Constructor cstr = cls.getDeclaredConstructor();
      cstr.setAccessible(true);
      result = cstr.newInstance();
    }

    ctx.getObjectPath().push(result);
    String hash = (String) jsonNode.getChildValue(HASH_KEY);
    if (hash != null)
      ctx.getHashToObjectMap().put(hash, result);

    Map<String, BeanProperty> pds = ClassUtil.getProperties(cls);

    JSONCoderOption opt = ctx.getOption();
    if (jsonNode.getChildren() == null)
      return result;  // Empty object

    for(TDNode nc : jsonNode.getChildren()){
      BeanProperty prop = pds.get(nc.getKey());
      if(prop == null)  // Certain serializer does't follow java bean naming convention, the attribute name is capitalized
        prop = pds.get(StringUtil.lowerFirst(nc.getKey()));

      if(prop == null){
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("No such attribute:" + nc.getKey() + ",class:" + cls);
        continue;
      }

      int mod = prop.getModifier();
      if(Modifier.isStatic(mod) || prop.isTransient()){
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("Field is static or transient:" + nc.getKey() + ",class:" + cls);
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
          throw new BeanCoderException("Field is not mutable:" + nc.getKey() + ",class:" + cls);
        continue;
      }

      Type childType = prop.getGenericType();
      Object child = ctx.decode(nc, childType, childTargetObj, nc.getKey());
      if(childTargetObj != child)
        prop.set(result, child);
    }
    return result;
  }
}