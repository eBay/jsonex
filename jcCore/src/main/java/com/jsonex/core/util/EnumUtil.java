/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.jsonex.core.annotation.DefaultEnum;
import com.jsonex.core.type.Identifiable;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.WeakHashMap;

@SuppressWarnings("ALL")
public class EnumUtil {
  private static final WeakHashMap<Class<Enum<?>>, Enum<?>> defaultEnumValues = new WeakHashMap<>();

  public static <K, T extends Enum<T> & Identifiable<K>> T getEnumById(Class<T> cls, K id, T defaultEnum) {
    for (T v : cls.getEnumConstants())
      if (v.getId().equals(id))
        return v;
    return defaultEnum;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <K, T extends Enum<T> & Identifiable<K>> T getEnumById(Class<T> cls, K id) {
    return getEnumById(cls, id, (T)getDefaultValue((Class)cls));  // Not sure why generic not working here 
  }
  
  /**
   * Similar to getEnumById, but it will convert the id to string before a string comparison. 
   */
  public static <K, T extends Enum<T> & Identifiable<K>> T getEnumByIdString(Class<T> cls, String id) {
    for (T v : cls.getEnumConstants())
      if (String.valueOf(v.getId()).equals(id))
        return v;
    return (T)getDefaultValue((Class)cls);
  }
  
  /**
   * Similar to Enum.valueOf(), but instead of throwing exception, this one will return defaultValue
   * if the value is invalid.
   */
  public static <T extends Enum<T>> T valueOf(Class<T> cls, String name, T defaultValue) {
    try{
      return Enum.valueOf(cls, name);
    }catch(IllegalArgumentException e){
      return defaultValue;
    }
  }

  /**
   * Similar to Enum.valueOf(), but instead of throwing exception, this one will return the annotated default value
   * if the value is invalid.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T extends Enum<T>> T valueOf(Class<T> cls, String name) { return valueOf(cls, name,  (T)getDefaultValue((Class)cls)); }
  
  public static Enum<?> getDefaultValue(Class<Enum<?>> cls) {
    Enum<?> result = defaultEnumValues.get(cls);
    if (result == null) {
      synchronized (defaultEnumValues) {
        result = getDefaultValueNoCache(cls);
        defaultEnumValues.put(cls, result);
      }
    }
    return result;
  }

  @SneakyThrows
  private static Enum<?> getDefaultValueNoCache(Class<Enum<?>> cls) {
    for (Enum<?> e : cls.getEnumConstants()) {
      for (Annotation anno : cls.getField(e.name()).getAnnotations()) {
        if (anno.annotationType() == DefaultEnum.class ||
            anno.annotationType().getName().equals("JsonEnumDefaultValue"))  // Jackson annotation
          return e;
      }
    }
    return null;
  }
  
  /**
   * Convert a long value to EnumSet, assume the id is represented as bit location
   */
  public static <T extends Enum<T> & Identifiable<Long>> EnumSet<T> toEnumSet(long f, Class<T> cls) {
    EnumSet<T> result = EnumSet.noneOf(cls);
    for (T flag : cls.getEnumConstants()) {
      if ((f & flag.getId()) != 0)
        result.add(flag);
    }
    return result;
  }

  /**
   * Convert enumset to long of bit array, assume the id is represented as bit location
   */
  public static <T extends Enum<T> & Identifiable<Long>> long toLong(EnumSet<T> enums) {
    long f = 0;
    for (Identifiable<Long> flag : enums)
      f |= flag.getId();
    return f;
  }
}
