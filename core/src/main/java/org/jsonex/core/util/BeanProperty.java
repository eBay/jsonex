/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Optional;
import java.util.function.Function;

import static org.jsonex.core.util.ArrayUtil.first;
import static org.jsonex.core.util.LangUtil.getIfInstanceOf;

/**
 * Wrapper around class getter, setter and field to provide convenient access of a bean property so that clients don't
 * need to worry about the actual representation (field or getter/setter) of the property.
 */
@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor @ToString
public class BeanProperty {
  @Getter final String name;
  @Getter Method setter;
  @Getter Method getter;
  @Getter Method hasChecker;
  @Getter Field field;
  
  public boolean isTransient() {
    if (field != null && Modifier.isTransient(field.getModifiers()))
        return true;

    // java.beans.Transient.class is not available in Android. we use name matching
    return getAnnotation("Transient") != null;
  }

  public boolean isImmutable(boolean allowPrivate){return setter == null && !isFieldAccessible(allowPrivate); }
  public boolean isReadable(boolean allowPrivate){return getter != null || isFieldAccessible(allowPrivate); }
  public boolean isFieldAccessible(boolean allowPrivate) {
    if (field == null)
      return false;
    return allowPrivate || Modifier.isPublic(field.getModifiers());
  }
  
  public void set(Object obj, Object value){
    try{
      if (setter != null){
        setter.setAccessible(true);
        setter.invoke(obj, value);
        return;
      } else if (field != null) {
        field.setAccessible(true);
        field.set(obj, value);
        return;
      }
    } catch(Exception e){
      throw new InvokeRuntimeException("error set value:" + name + ", class=" + obj.getClass() + ",value=" + value, e);
    }
    throw new InvokeRuntimeException("field is not mutable: " + name + ", class:" + obj.getClass());
  }
  
  public Object get(Object obj){
    try {
      if (hasChecker != null && Boolean.FALSE.equals(hasChecker.invoke(obj)))
        return null;
      if (getter != null) {
        getter.setAccessible(true);
        return getter.invoke(obj);
      } else if (field != null) {
        field.setAccessible(true);
        return field.get(obj);
      }
    } catch(Exception e) {
      throw new InvokeRuntimeException("error get value:" + name + ", class:" + obj.getClass(), e);
    }
    throw new InvokeRuntimeException("field is not readable: " + name + ", class:" + obj.getClass());
  }
  
  public <T extends Annotation> T getAnnotation(Class<T> cls) {
    T result;
    if (getter != null) {
      result = getter.getAnnotation(cls);
      if (result != null)
        return result;
    }
    
    if (field != null) {
      result = field.getAnnotation(cls);
      if (result != null)
        return result;
    }
    
    if ( setter != null)
      return setter.getAnnotation(cls);
    
    return null;
  }

  public Annotation getAnnotation(String name) {
    Optional<Annotation> result;
    if (getter != null) {
      result = getAnnotation(getter.getAnnotations(), name);
      if (result.isPresent())
        return result.get();
    }
    if (field != null) {
      result = getAnnotation(field.getAnnotations(), name);
      if (result.isPresent())
        return result.get();
    }
    if (setter != null)
      return getAnnotation(setter.getAnnotations(), name).orElse(null);
    return null;
  }

  private Optional<Annotation> getAnnotation(Annotation[] annot, String name) {
    return first(annot, a -> a.annotationType().getSimpleName().equals(name));
  }

  public Type getGenericType(){
    if (getter != null)
      return getter.getGenericReturnType();
    else if (field != null)
      return field.getGenericType();
    else
      return setter.getGenericParameterTypes()[0];
  }

  /** If the genericType is TypeVariable, it will resolve it with actual type. otherwise it's the same as getGenericType */
  public Type getActualGenericType(Type clsType) {
    return  getIfInstanceOf(getGenericType(), TypeVariable.class,
        t -> ClassUtil.getActualTypeOfTypeVariable(t, clsType), Function.identity());
  }
  
  public Class<?> getType(){
    if (getter != null)
      return getter.getReturnType();
    else if (field != null)
      return field.getType();
    else
      return setter.getParameterTypes()[0];
  }
  
  public int getModifier(){
    if (getter != null)
      return getter.getModifiers();
    else if (field != null)
      return field.getModifiers();
    else
      return setter.getModifiers();
  }
}