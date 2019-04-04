/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncoder.coder.CoderArray;
import com.ebay.jsoncoder.coder.CoderCollection;
import com.ebay.jsoncoder.coder.CoderMap;
import com.ebay.jsoncoder.coder.CoderObject;
import com.ebay.jsoncodercore.util.ClassUtil;
import com.ebay.jsoncodercore.util.StringUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static com.ebay.jsoncodercore.util.ClassUtil.convertToSimpleType;

/**
 * A coder to convert java class to a Map, List or String representation.
 *
 * <p>For an array or collection object, it will be encoded as List
 * <p>For Java Beans, it will be converted into Map, the key is of String type (The property name)
 * <p>For some simple types such as Date, int, it will be converted into a String.
 *
 * <p>As this encoder won't store any type information, it will only infer the type from the class.
 * For collections fields, the element type information has to be specified with generic type.
 */
@SuppressWarnings({"WeakerAccess"})
public class BeanCoder {
  private final static int MAX_OBJECTS = 10_000;
  private final static int MAX_DEPTH = 50;
  public final static String HASH_KEY = "$hash";
  private final static String REF_PREFIX = "$ref:";
  private final static JSONCoder jsonCoder;
  static {
    JSONCoderOption coderOption = new JSONCoderOption();
    coderOption.getJsonOption().setAlwaysQuoteName(true);
    jsonCoder = new JSONCoder(coderOption);
  }

  // Convenient utility method
  public static Object encode(Object obj) { return encode(obj, new BeanCoderContext(JSONCoderOption.global), null); }
  public static Object encode(Object obj, BeanCoderContext context, Type type) { return _encode(obj, context.reset(), type); }

  public static Object decode(Object obj, Type type) { return decode(obj, type, null, "", new BeanCoderContext(JSONCoderOption.global)); }
  public static <T> T decode(Object obj, T target) {
    return (T)decode(encode(obj), target.getClass(), target, "", new BeanCoderContext(JSONCoderOption.global));
  }

  @SuppressWarnings("unchecked")
  public static <T> T deepClone(T src) { return src == null ? null : (T)decode(encode(src), src.getClass()); }
  public static <T> T deepCopyTo(T src, T target) { return src == null ? null : decode(src, target); }

  /**
   * This method should only be called internally during recursion, as it will not reset context
   *
   * @return  the encoded object, can be either LinkedHashMap, ArrayList, String, primitive types or null
   */
  @SuppressWarnings("unchecked")
  static Object _encode(Object obj, BeanCoderContext ctx, Type type)
  {
    JSONCoderOption opt = ctx.getOption();
    int pathSize = ctx.objectPath.size();
    try {
      if (obj == null)
        return null;

      // For reflection Objects, we will just dump it's string value, no deserialization is possible
      if(obj.getClass().getName().startsWith("java.lang.reflect."))
        return obj.toString();

      Class<?> cls = obj.getClass();  // Use the real object;

      //Handle primitive types
      if (ClassUtil.isSimpleType(cls))
        return obj;

      // Filter ignored classes
      if (opt.isClassSkipped(cls))
        return null;


      @SuppressWarnings("rawtypes")
      ICoder coder = opt.findCoder(cls);
      if (coder != null) {
        return coder.encode(obj, type, ctx);
      }

      if(cls == Object.class || cls == BigDecimal.class)
        return obj.toString();

      Object eqWrapper = opt.getEqualsWrapper(obj);

      //Prevent too many objects or cyclic reference.
      try {
        int p = ctx.objectPath.indexOf(eqWrapper);
        if (p >= 0) {
          return REF_PREFIX + StringUtil.appendRepeatedly(new StringBuilder(), "../", p + 1);
        }
      } catch(ClassCastException e) {
        // Workaround for some class that breaks equals() contract by doing caste before type check
        // We will ignore this error
      }

      if (ctx.convertedObjects.size() > MAX_OBJECTS || ctx.objectPath.size() > MAX_DEPTH)
        return "[TRIMMED_DUE_TO_TOO_MANY_OBJECT]";

      Map<String, Object> orgResult = ctx.convertedObjects.get(eqWrapper);
      if(opt.dedupWithRef && orgResult != null) {
        String hash = (String) orgResult.get(HASH_KEY);
        if (hash == null) {  // This is the first reference. Only if there's a reference, the original map will display the hash value
          hash = Long.toHexString(abs(eqWrapper.hashCode()));
          orgResult.put(HASH_KEY, hash);
        }
        return REF_PREFIX + "#" + hash;
      }

      //Don't put empty collection and map as the hashCode method is not implemented properly
      if (! ((obj instanceof Collection) && ((Collection<?>)obj).isEmpty() ||
          (obj instanceof Map) && ((Map<?, ?>)obj).isEmpty())) {
        ctx.objectPath.push(eqWrapper);
      }

      Object result;
      if (cls.isArray()) {   //Handle it as any array
        result = CoderArray.getInstance().encode(obj, type, ctx);
      } else if (obj instanceof Collection) {  //Handle it as an Collection
        result = CoderCollection.getInstance().encode((Collection<?>)obj, type, ctx);
      } else if (obj instanceof Map) {
        result = CoderMap.getInstance().encode((Map<?,?>)obj, type, ctx);
      } else
        result = (Map<String, Object>)CoderObject.getInstance().encode(obj, type, ctx);

      if (result instanceof Map && !((Map)result).isEmpty())
        ctx.convertedObjects.put(eqWrapper, (Map)result);

      return result;
    } catch (Exception ex) {
      throw new BeanCoderException(ex);
    } finally{
      if(ctx.objectPath.size() > pathSize) {
          ctx.objectPath.pop();
      }
    }
  }

  /**
   *
   * @param obj  The object to decode, the o can be one of following type: LinkedHashMap, ArrayList, String, primitive types or null
   * @param type  The target type to decode to
   * @param targetObj The target object to decode to, if it's null, a new Object will be created
   * @param ctx  The decode context
   * @return The decoded Object
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Object decode(Object obj, Type type, Object targetObj, String name, BeanCoderContext ctx)
  {
    if(obj == null)
      return null;
    Class<?> cls = ClassUtil.getGenericClass(type);

    int pathSize = ctx.objectPath.size();
    try{
      if (obj instanceof String && cls != String.class) {  // handle $ref type
        String s = (String) obj;
        if (s.startsWith(REF_PREFIX)) {
          String ref = s.substring(REF_PREFIX.length());
          if(ref.length() > 0 && ref.charAt(0) == '#')  // Reference to a hashcode
            return ctx.hashToObjectMap.get(ref.substring(1));

          // Assume ref is in the format of "../../" etc.
          int level = ref.length() / 3;
          if (ctx.objectPath.size() < level)
            throw new BeanCoderException("Reference level exceeding objectPath:" + ctx.objectPath + "; ref:" + ref);
          return ctx.objectPath.get(level - 1);
        }
      }

      Object p = convertToSimpleType(obj, cls);
      if (p != null)
        return p;

      ICoder<?> coder = ctx.option.findCoder(cls);
      if(coder != null)
        return coder.decode(obj, type, targetObj, ctx);

      if (cls == String.class) {  // expect a string, but got non-string, just return serialized form of the the object
        return jsonCoder.encode(obj);
      }

      if (cls.isArray())  // Handle the array type
        return CoderArray.getInstance().decode(obj, type, targetObj, ctx);

      if (Collection.class.isAssignableFrom(cls))  // Handle the collection type
        return CoderCollection.getInstance().decode(obj, type, targetObj, ctx);

      if (Map.class.isAssignableFrom(cls))
        return CoderMap.getInstance().decode(obj, type, targetObj, ctx);

      //Handle bean type
      return CoderObject.getInstance().decode(obj, type, targetObj, ctx);
    } catch(Exception e) {
      throw new BeanCoderException("failed to decode:"+type + "; name=" + name, e);
    } finally {
      if(ctx.objectPath.size() > pathSize)
        ctx.objectPath.pop();
    }
  }

  //Create this method just to avoid findbug issues.
  private static int abs(int a) { return a < 0 ? -a : a; }
}
