/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

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
  @Getter Field field;
  
  public boolean isTransient() {
    if (field != null && Modifier.isTransient(field.getModifiers()))
        return true;

    return getAnnotation(java.beans.Transient.class) != null;
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
      if(setter != null){
        setter.setAccessible(true);
        setter.invoke(obj, value);
        return;
      }else if(field != null) {
        field.setAccessible(true);
        field.set(obj, value);
        return;
      }
    }catch(Exception e){
      throw new InvokeRuntimeException("error set value:" + name + ",class=" + obj.getClass() + ",value=" + value, e);
    }
    throw new InvokeRuntimeException("field is not mutable: " + name + ",class:" + obj.getClass());
  }
  
  public Object get(Object obj){
    try {
      if(getter != null) {
        getter.setAccessible(true);
        return getter.invoke(obj);
      } else if(field != null) {
        field.setAccessible(true);
        return field.get(obj);
      }
    }catch(Exception e) {
      throw new InvokeRuntimeException("error get value:" + name + ",class:" + obj.getClass(), e);
    }
    throw new InvokeRuntimeException("field is not readable: " + name + ",class:" + obj.getClass());
  }
  
  public <T extends Annotation> Annotation getAnnotation(Class<T> cls) {
    Annotation result;
    if(getter != null) {
      result = getter.getAnnotation(cls);
      if (result != null)
        return result;
    }
    
    if(field != null) {
      result = field.getAnnotation(cls);
      if (result != null)
        return result;
    }
    
    if( setter != null)
      return setter.getAnnotation(cls);
    
    return null;
  }
  
  public Type getGenericType(){
    if(getter != null)
      return getter.getGenericReturnType();
    else if(field != null)
      return field.getGenericType();
    else
      return setter.getGenericParameterTypes()[0];
  }
  
  public Class<?> getType(){
    if(getter != null)
      return getter.getReturnType();
    else if(field != null)
      return field.getType();
    else
      return setter.getParameterTypes()[0];
    
  }
  
  public int getModifier(){
    if(getter != null)
      return getter.getModifiers();
    else if(field != null)
      return field.getModifiers();
    else
      return setter.getModifiers();
  }
}