/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.coder;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.BeanProperty;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.core.util.StringUtil;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.BeanCoderException;
import org.jsonex.jsoncoder.ICoder;
import org.jsonex.jsoncoder.JSONCoderOption;
import org.jsonex.jsoncoder.fieldTransformer.FieldTransformer.FieldInfo;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.json.TDJSONWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.jsonex.core.util.LangUtil.getIfInstanceOf;
import static org.jsonex.core.util.ListUtil.removeLast;

@Slf4j
public class CoderObject implements ICoder<Object> {
  public static final InjectableInstance<CoderObject> it = InjectableInstance.of(CoderObject.class);
  public static CoderObject get() { return it.get(); }

  private final static String TYPE_KEY = "$type";

  @Override public Class<Object> getType() { return Object.class; }

  @Override public TDNode encode(Object obj, Type type, BeanCoderContext ctx, TDNode target) {
    target.setType(TDNode.Type.MAP);
    JSONCoderOption opt = ctx.getOption();

    Class<?> cls = obj.getClass();  // Use the real object;
    if (opt.isIgnoreSubClassFields(cls) && type != null)
      cls = ClassUtil.getGenericClass(type);

    if (opt.isShowType() || cls != obj.getClass())
      target.createChild(TYPE_KEY).setValue(obj.getClass().getName());

    Map<String, BeanProperty> pds = ClassUtil.getProperties(cls);
    for (BeanProperty pd : pds.values()) {
      if (!pd.isReadable(opt.isShowPrivateField()))
        continue;

      if (pd.isImmutable(opt.isShowPrivateField()) && opt.isIgnoreReadOnly())
        continue;  // Only mutable attribute will be encoded

      if (pd.isTransient())
        continue;

      // V3DAL will cause Lazy load exception, we have to catch it
      try {
        FieldInfo fieldInfo = new FieldInfo(pd.getName(), pd.getActualGenericType(type), pd.get(obj));
        fieldInfo = opt.transformField(cls, fieldInfo, ctx);

        if (fieldInfo.getName() == null)  // Skipped
          continue;

        if (fieldInfo.getObj() != null) {
          Type fieldType =  getIfInstanceOf(fieldInfo.getType(), TypeVariable.class,
              t -> ClassUtil.getActualTypeOfTypeVariable(t, type), Function.identity());
          TDNode cn = ctx.encode(fieldInfo.getObj(), fieldType, target.createChild(pd.getName()));
          if (cn.getType() == TDNode.Type.SIMPLE && cn.getValue() == null)
            removeLast(target.getChildren());
        }
      } catch(Exception e) {
        opt.getWarnLogLevel().log(log, "warning during encoding", e);
        // ignore this exception
      }
    }
    return target;
  }

  @Override @SneakyThrows
  public Object decode(TDNode tdNode, Type type, Object targetObj, BeanCoderContext ctx) {
    Class<?> cls = ClassUtil.getGenericClass(type);

    if (tdNode.getType() != TDNode.Type.MAP) {
      if (tdNode.getValue() != null && cls.isAssignableFrom(tdNode.getValue().getClass()))
        return tdNode.getValue();  // SIMPLE type
      if (cls.isAssignableFrom(TDNode.class))
        return tdNode;  // If cls is Object.class, we don't do further decoding
      throw new BeanCoderException("Expect an Map object, but actual type=" + tdNode.getType() +
          ";o=" + StringUtil.toTrimmedStr(TDJSONWriter.get().writeAsString(tdNode) + ": pos=" + tdNode.getStart(), 500));
    }

    Object subType = tdNode.getChildValue(TYPE_KEY);
    if (subType instanceof String) {
      if (!ctx.getOption().isAllowPolymorphicClasses())
        throw new BeanCoderException(
            "allowPolymorphicClasses is not enabled in option while there's $type attributes: " + subType);
      try {
        Class<?> loadedClass = Class.forName((String) subType);
        if (!cls.isAssignableFrom(loadedClass)) {
          throw new BeanCoderException(
              "Specified class:" + loadedClass.getName() + " is incompatible to destination class:" + cls.getName());
        }
        cls = loadedClass;
      } catch(ClassNotFoundException e) {
        throw new BeanCoderException("Incorrect $type:" + subType, e);
      }
    }

    // TODO: confirm the test case
    if (cls.isAssignableFrom(TDNode.class))
      return tdNode;

    Object result = targetObj;
    if (result == null) {
      Constructor cstr = cls.getDeclaredConstructor();
      cstr.setAccessible(true);
      result = cstr.newInstance();
    }

    ctx.getNodeToObjectMap().put(tdNode, result);

    Map<String, BeanProperty> pds = ClassUtil.getProperties(cls);

    JSONCoderOption opt = ctx.getOption();
    if (tdNode.getChildren() == null)
      return result;  // Empty object

    for (TDNode nc : tdNode.getChildren()) {
      BeanProperty prop = pds.get(nc.getKey());
      if (prop == null)  // Certain serializer does not follow java bean naming convention, the attribute name is capitalized
        prop = pds.get(StringUtil.lowerFirst(nc.getKey()));

      if (prop == null) {
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("No such attribute:" + nc.getKey() + ",class:" + cls);
        continue;
      }

      int mod = prop.getModifier();
      if (Modifier.isStatic(mod) || prop.isTransient()) {
        if (opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("Field is static or transient:" + nc.getKey() + ",class:" + cls);
        continue;  // None public, or static, transient
      }

      Object childTargetObj = null;

      if (prop.isReadable(true)
          && !ClassUtil.isSimpleType(prop.getType())
          && !Enum.class.isAssignableFrom(prop.getType())
          && !Date.class.isAssignableFrom(prop.getType()))
        // TODO: Exclude more non-container and java bean types to prevent side effect when calling getter method
        // One example of the site effect for getter method is calculated value for example: calculate hash code
        try {
          childTargetObj = prop.get(result);
        } catch (Exception e) {
          // ignore if calling get throws exception
        }

      if (childTargetObj == null && prop.isImmutable(true)) {  // In that case, the attribute has to be mutable.
        if(opt.isErrorOnUnknownProperty())
          throw new BeanCoderException("Field is not mutable:" + nc.getKey() + ",class:" + cls);
        continue;
      }

      Type childType = prop.getActualGenericType(type);
      Object child = ctx.decode(nc, childType, childTargetObj, nc.getKey());
      if (!Objects.equals(childTargetObj, child))
        prop.set(result, child);
    }
    return result;
  }
}