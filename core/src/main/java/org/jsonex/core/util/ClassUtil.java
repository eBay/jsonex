/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.jsonex.core.type.Nullable;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("ALL")
public class ClassUtil {
  private static final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

  public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];


  @Deprecated  //Used only for testing, don't use in production code
  @SneakyThrows
  public static void setStaticPrivateField(String className, String fieldName, Object value) {
    Class<?> cls = Class.forName(className, true, ClassUtil.class.getClassLoader());
    setPrivateField(cls, null, fieldName, value);
  }

  @Deprecated  //Used only for testing, don't use in production code
  public static void setPrivateField(Object obj, String fieldName, Object value) { setPrivateField(obj.getClass(), obj, fieldName, value); }

  @Deprecated  //Used only for testing, don't use in production code
  @SneakyThrows
  public static void setPrivateField(Class<?> cls, Object obj, String fieldName, Object value){
    Field field = getDeclaredField(cls, fieldName);

    //Very Hacky
//    Field modifiersField = Field.class.getDeclaredField("modifiers");
//    modifiersField.setAccessible(true);
//    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.setAccessible(true);
    field.set(obj, value);
  }

  /**
   * Class.getDeclaredField won't search the fields from base class.
   * This class will recursively search the base classes.
   */
  public static Field getDeclaredField(Class<?> cls, String name) throws NoSuchFieldException{
    for(; cls != null && cls != Object.class; cls = cls.getSuperclass()){
      try{
        return cls.getDeclaredField(name);
      }catch(NoSuchFieldException e){
        //Ignored, continue with super class.
      }
    }
    throw new NoSuchFieldException(name);
  }

  /**
   * As Java Introspector.getBeanInfo(cls) return the attributes are not in order
   * a custom implementation here to make it the same order as the class definition.
   * Also it will return all the attributes include super class one.
   * The order is mainly based on field order.
   *
   * <p>Note: The methods order return by Java reflection is not always following source order
   *
   * <P>The BeanProperties are cached
   */
  public static Map<String, BeanProperty> getProperties(Class<?> cls) {
    Map<String, BeanProperty> result = beanPropertyCache.get(cls);
    if (result == null) {
      synchronized(beanPropertyCache) {
        result = _getProperties(cls);
        beanPropertyCache.put(cls, result);
      }
    }
    return result;
  }

  private static final WeakHashMap<Class<?>, Map<String, BeanProperty>> beanPropertyCache = new WeakHashMap<>();

  private static Map<String, BeanProperty> _getProperties(Class<?> cls) {
    // Find all the getter/setter methods
    // Use TreeMap as method is not in stable order in JVM implementations, so we need to sort them to make stable order
    Map<String, BeanProperty> attributeMap = new TreeMap<>();
    for (Method m : getAllMethods(cls)) {
      int mod = m.getModifiers();
      if(!Modifier.isPublic(mod) || Modifier.isStatic(mod))
        continue;  //None public, or static, transient

      String name = null;
      boolean isSetter = false;
      if (m.getName().startsWith("get"))
        name = m.getName().substring(3);
      else if(m.getName().startsWith("is")) {
        if(m.getReturnType() != Boolean.class && m.getReturnType() != boolean.class)
          continue;
        name = m.getName().substring(2);
      } else if(m.getName().startsWith("set")) {
        isSetter = true;
        name = m.getName().substring(3);
      }else
        continue;

      if (!isSetter && m.getParameterTypes().length != 0)
        continue;

      if (isSetter && m.getParameterTypes().length != 1)
        continue;

      if (name.length() == 0)  // JSON doesn't allow empty key, we replace it with ^
        name = "^";

      name = StringUtil.lowerFirst(name);
      if(name.equals("class"))
        continue;

      BeanProperty prop = attributeMap.get(name);
      if(prop == null){
        prop = new BeanProperty(name);
        attributeMap.put(name, prop);
      }

      if(isSetter)
        prop.setter = m;
      else {
        // For union type in certain framework, isXXX is to indicate if the attribute is available, we will override it
        // with the actual getter method
        if (prop.getter == null || prop.getter.getReturnType() == Boolean.TYPE)
          prop.getter = m;
      }
    }

    // Check fields, reorder based on field order.
    Map<String, BeanProperty> fieldMap = new LinkedHashMap<>();
    for(Field f : getAllFields(cls)){
      int mod = f.getModifiers();
      if(Modifier.isStatic(mod))
        continue;

      String name = f.getName();
      if (name.startsWith("m_"))  // Remove prefix in case it follows legacy naming convension
        name = name.substring(2);
      if (name.startsWith("_"))  // Remove prefix in case it follows legacy naming convension
        name = name.substring(1);

      // Field names may not be unique if the same name is defined in the base class
      // We skip those duplicated names for re-ordering
      if (fieldMap.containsKey(name)) {
        fieldMap.get(name).field = f;
        continue;
      }

      BeanProperty prop = attributeMap.remove(name);
      if (prop == null) {
        prop = new BeanProperty(name);
      }
      fieldMap.put(name, prop);
      prop.field = f;
    }

    fieldMap.putAll(attributeMap);  // Add back those setter/getter with field.
    return fieldMap;
  }

  /**
   * Get the object using a Object Path
   * Object path has following format
   *
   * <p>FullQualifiedClassName/variable.variable.index.variable
   *
   * <p>For example:
   *   <p>com.sample.StateConfig/instance.statePartiesMap.10.value
   *
   * <p>The sequence we search top level variable:
   * <li>1. Static getter method
   * (For the top class, the class has to have a default public constructor for remain steps)
   * <li>2. non-static getter method
   * <li>3. Static field.
   * <li>4. non-state field. (No matter it's public or private.)
   *
   * // TODO: support property name contains "." or "/"
   */
  public static Object getObjectByPath(String path) throws Exception {
    int p = path.indexOf('/');//NOPMD
    if (p < 0)
      throw new IllegalArgumentException("Path has to include: '/'");

    String relativePath = path.substring(p+1);
    if(relativePath.length() == 0)
      throw new IllegalArgumentException("Path should have at least one variable section");

    return getObjectByPath(Class.forName(path.substring(0, p)), null, relativePath);
  }

  public static Object getObjectByPath(@Nullable Class<?> cls, @Nullable Object obj, String relativePath) {
    return getObjectByPath(cls, obj, ListUtil.listOf(relativePath.split("\\.")));
  }

  public static Object getObjectByPath(@Nullable Class<?> cls, @Nullable Object obj, List<String> relativePath) {
    Object result = getPropertyValue(cls, obj, relativePath.get(0));
    relativePath.remove(0);
    if(!relativePath.isEmpty())
      result = getObjectByPath(null, result, relativePath);
    return result;
  }

  /**
   * Get a property for a class or Object
   * It will try getter method first, then try field
   */
  @SneakyThrows
  public static @Nullable  Object getPropertyValue(@Nullable Class<?> cls, @Nullable Object obj, String propertyName)
  {
    if (cls == null && obj == null)
      return null;

    if (cls == null )
      cls = obj.getClass();

    //Check if it's array
    try{
      int idx = Integer.valueOf(propertyName);
      //it has to be a map, collection or array and Object can't be null
      if (obj == null)
        throw new IllegalArgumentException("obj can't be null when property name is a number: " + propertyName);
      if (cls.isArray()) {
        return Array.get(obj, idx);
      } else if(Collection.class.isAssignableFrom(cls)) {
        int i=0;
        for (Object o : (Collection<?>)obj) {
          if(idx == i++)
            return o;
        }
        throw new ArrayIndexOutOfBoundsException("index out of bounds: expected idx=" + idx + "; actual size=" + i);
      } else if(Map.class.isAssignableFrom(cls)) {
        int i=0;
        for (Map.Entry<?,?> e : ((Map<?,?>)obj).entrySet()) {
          if(idx == i++)
            return e;
        }
        throw new ArrayIndexOutOfBoundsException("index out of bounds: expected idx=" + idx + "; actual size=" + i);
      }
      throw new IllegalArgumentException("obj type has to be Array, Collection or Map when property name is a number: " + propertyName);
    } catch (NumberFormatException e) {
      //ignore, continue;
    }

    if (Map.class.isAssignableFrom(cls))
      return ((Map<?,?>) obj).get(propertyName);

    //Try getter method first
    try {
      if ("^".equals(propertyName))  // special handling for "^" as placeholder for ""
        propertyName = "";
      String getMethodName = "get" + StringUtil.upperFirst(propertyName);
      Method method = cls.getMethod(getMethodName);
      int mod = method.getModifiers();
      //if(!Modifier.isPublic(mod))  //Comment it out as it's possible Object itself is not Accessible
      method.setAccessible(true);

      if (Modifier.isStatic(mod))
        return method.invoke(null);

      //It's a non-static method, need to access instance
      if (obj == null)
        obj = cls.newInstance();
      return method.invoke(obj);
    } catch(NoSuchMethodException e) {
      Field field = getDeclaredField(cls, propertyName);  //Only this method will return in-visible fields.

      if (field == null)
        throw new NoSuchFieldException(propertyName + " in class: " + cls.getName());

      int mod = field.getModifiers();
      //if(!Modifier.isPublic(mod))
      field.setAccessible(true);

      if (Modifier.isStatic(mod)) {
        return field.get(null);
      }

      //It's a non-static method, need to access instance
      if (obj == null)
        obj = cls.newInstance();
      return field.get(obj);
    }
  }

  public static Object getPropertyValue( @Nullable Object obj, String propertyName) {
    return getPropertyValue(null, obj, propertyName);
  }

  public static Object getPropertyValue( @Nullable Class<?> cls, String propertyName) {
    return getPropertyValue(cls, null, propertyName);
  }

  public static @Nullable Class<?> getGenericClass(@Nullable Type type) {
    if (type == null)
      return null;
    if (type instanceof Class<?>)
      return (Class<?>)type;
    if (type instanceof ParameterizedType)
      return (Class<?>)(((ParameterizedType)type).getRawType());
    if (type instanceof WildcardType) {
      WildcardType wt = (WildcardType) type;
      if(wt.getUpperBounds().length > 0)
        return getGenericClass(wt.getUpperBounds()[0]);
      return Object.class;
    }
    if (type instanceof TypeVariable) {
      TypeVariable<?> tv = (TypeVariable<?>) type;
      if(tv.getBounds().length > 0)
        return getGenericClass(tv.getBounds()[0]);
      return Object.class;
    }

    if (type instanceof GenericArrayType) {
      // TODO: Cache these empty arrays
      return Array.newInstance(getGenericClass(((GenericArrayType)type).getGenericComponentType()), 0).getClass();
    }

    throw new RuntimeException("Unexpected type: cls=" + type.getClass() + "; string=" + type);
  }

  public static @Nullable Type[] getGenericTypeActualParams(@Nullable Type type) {
    if(type == null || type instanceof Class)
      return null;
    if(type instanceof ParameterizedType)
      return ((ParameterizedType)type).getActualTypeArguments();
    throw new RuntimeException("Unexpected type:" + type);
  }

  public static Type[] getGenericTypeActualParamsForInterface(Type type, Class<?> intf) {
    Type[] intfs = getGenericClass(type).getGenericInterfaces();
    for (Type t : intfs)
      if (getGenericClass(t) == intf)
        return getGenericTypeActualParams(t);
    return EMPTY_TYPE_ARRAY;
  }

  public static List<Class<?>> getAllInterface(Class<?> cls) {
    List<Class<?>> result = new ArrayList<Class<?>>();
    for(; cls != Object.class; cls = cls.getSuperclass())
      result.addAll(Arrays.asList(cls.getInterfaces()));
    return result;
  }

  public static Class<?> findInterface(Class<?> cls, Class<?> itf) {
    for(; cls != Object.class; cls = cls.getSuperclass())
      for(Class<?> i : cls.getInterfaces())
        if(itf.isAssignableFrom(i))
          return i;
    return null;
  }

  public static Field[] getAllFields(Class<?> cls) {
    List<Field> result = new ArrayList<Field>();
    for(; cls != Object.class && cls != null; cls = cls.getSuperclass())  // Interface superclass is null
      result.addAll(0, Arrays.asList(cls.getDeclaredFields()));
    return result.toArray(new Field[0]);
  }

  public static Method[] getAllMethods(Class<?> cls) {
    List<Method> result = new ArrayList<Method>();
    for(; cls != Object.class && cls != null; cls = cls.getSuperclass())  // Interface superclass is null
      result.addAll(0, Arrays.asList(cls.getDeclaredMethods()));
    return result.toArray(new Method[0]);
  }

  public static <T extends Enum<T>> T stringToEnum(Class<T> cls, String str) {
    try{
      int i = Integer.parseInt(str);
      return cls.getEnumConstants()[i];
    }catch(NumberFormatException e){
      return Enum.valueOf(cls, str);
    }
  }

  /**
   * If str is null, NPE will be thrown. This method will handle simple type conversion,
   * if a type is not supported, it will return null.
   *
   * @param str The string value
   * @param cls The target class
   * @param ctx The conversion context
   *
   * @return null means the type is not supported.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> T stringToSimpleObject(String str, Class<T> cls, BeanConvertContext ctx)
  {
    if(str == null)
      throw new NullPointerException();//NOPMD

    if(cls == String.class)
      return (T)str;

    str = str.trim();

    //Handle primitive types
    if(cls == boolean.class || cls == Boolean.class)
      return (T)Boolean.valueOf(str);

    if(cls == byte.class || cls == Byte.class)
      return (T)Byte.valueOf(str);

    if(cls == int.class || cls == Integer.class)
      return (T)Integer.valueOf(str);

    if(cls == long.class || cls == Long.class)
      return (T)Long.valueOf(str);

    if(cls == float.class || cls == Float.class)
      return (T)Float.valueOf(str);

    if(cls == double.class || cls == Double.class)
      return (T)Double.valueOf(str);

    if(cls == char.class || cls == Character.class)
      return (T)Character.valueOf(str.charAt(0));

    if(Enum.class.isAssignableFrom(cls)){
      return (T)stringToEnum((Class)cls, str);
    }

    if(Date.class.isAssignableFrom(cls))
      try{
        return (T)new SimpleDateFormat(ctx.dateFormat).parse(str);
      }catch(ParseException e){
        throw new RuntimeException(e);
      }

    return null;
  }

  /**
   * Check if the cls is Simple type that can be directly mapped to JSON type
   */
  public static boolean isSimpleType(Class<?> cls){
    return cls != null && (
        cls.isPrimitive() ||
            cls == Boolean.class ||
            cls == Byte.class ||
            cls == Character.class ||
            cls == Integer.class ||
            cls == Long.class ||
            cls == Float.class ||
            cls == Double.class ||
            cls == String.class);
  }

  /**
   * Convert to a simple type from a matching Object
   * return null, if it's not able to convert
   */
  public static @Nullable Object objectToSimpleType(@Nullable Object obj, Class<?> cls)
  {
    if (cls == String.class && (obj == null || obj instanceof String))
      return obj;

    //Handle primitive types
    if (cls == boolean.class || cls == Boolean.class)
      return obj;

    if (cls == byte.class || cls == Byte.class)
      return ((Number)obj).byteValue();

    if (cls == int.class || cls == Integer.class)
      return ((Number)obj).intValue();

    if (cls == long.class || cls == Long.class)
      return ((Number)obj).longValue();

    if (cls == float.class || cls == Float.class)
      return ((Number)obj).floatValue();

    if (cls == double.class || cls == Double.class)
      return ((Number)obj).doubleValue();

    if (cls == char.class || cls == Character.class)
      return obj instanceof Character ? (Character) obj : ((String)obj).charAt(0);

    if (cls == BigDecimal.class)
      return new BigDecimal(String.valueOf(obj));

    return null;
  }

  public static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<>();//NOPMD
  private static void addPrimitiveType(Class<?> cls) { PRIMITIVE_TYPE_MAP.put(cls.getName(), cls); }
  static {
    addPrimitiveType(Boolean.TYPE);
    addPrimitiveType(Short.TYPE);
    addPrimitiveType(Integer.TYPE);
    addPrimitiveType(Long.TYPE);
    addPrimitiveType(Float.TYPE);
    addPrimitiveType(Double.TYPE);
    addPrimitiveType(Character.TYPE);
  }

  public static Type getType(String typeName) throws Exception {
    Class<?> primitiveType = PRIMITIVE_TYPE_MAP.get(typeName);
    if (primitiveType != null)
      return primitiveType;
    if(typeName.indexOf('/') >= 0)   // This is only for primitive types such as java.lang.Integer/TYPE
      return (Type)ClassUtil.getObjectByPath(typeName);
    else
      return Class.forName(typeName); // TODO: interpret generate type syntax
  }

  /**
   * This method will return the nearest caller stack above thisClass
   */
  public static StackTraceElement findCallerStackTrace(Class<?> calleeClass) {
    boolean findSelf = false;
    for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
      boolean isSelf = s.getClassName().equals(calleeClass.getName());
      if (!findSelf) {
        if (isSelf)
          findSelf = true;
      } else if (!isSelf)
        return s;
    }
    logger.error("Can't find the caller stack, return a dummy Stacktrace");  // Shouldn't happen
    return new StackTraceElement("unknown", "unknown","unknown", 0);  // Should teach here, return the top just to avoid NPE for caller
  }
}
