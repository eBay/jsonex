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
import com.ebay.jsoncoder.treedoc.TDJSONWriter;
import com.ebay.jsoncoder.treedoc.TDNode;
import com.ebay.jsoncodercore.util.ClassUtil;
import com.ebay.jsoncodercore.util.StringUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static com.ebay.jsoncodercore.util.ClassUtil.objectToSimpleType;

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

  // Convenient utility method
  public static TDNode encode(Object obj) { return encode(obj, new BeanCoderContext(JSONCoderOption.global), null); }
  public static TDNode encode(Object obj, BeanCoderContext context, Type type) { return _encode(obj, context.reset(), type, new TDNode()); }

  public static Object decode(TDNode obj, Type type) { return decode(obj, type, null, "", new BeanCoderContext(JSONCoderOption.global)); }
  public static <T> T decode(TDNode obj, T target) {
    return (T)decode(obj, target.getClass(), target, "", new BeanCoderContext(JSONCoderOption.global));
  }

  @SuppressWarnings("unchecked")
  public static <T> T deepClone(T src) { return src == null ? null : (T)decode(encode(src), src.getClass()); }
  public static <T> T deepCopyTo(T src, T target) { return src == null ? null : decode(encode(src), target); }

  /**
   * This method should only be called internally during recursion, as it will not reset context
   *
   * @return  the encoded object, can be either LinkedHashMap, ArrayList, String, primitive types or null
   */
  @SuppressWarnings("unchecked")
  static TDNode _encode(Object obj, BeanCoderContext ctx, Type type, TDNode target)
  {
    JSONCoderOption opt = ctx.getOption();
    int pathSize = ctx.objectPath.size();
    try {
      if (obj == null)
        return target;

      // For reflection Objects, we will just dump it's string value, no deserialization is possible
      Class<?> cls = obj.getClass();  // Use the real object;
      if(obj.getClass().getName().startsWith("java.lang.reflect.") ||
          cls == Object.class || cls == BigDecimal.class)
        return target.setValue(obj.toString());

      //Handle primitive types
      if (ClassUtil.isSimpleType(cls))
        return target.setValue(obj);

      // Filter ignored classes
      if (opt.isClassSkipped(cls))
        return target;

      @SuppressWarnings("rawtypes")
      ICoder coder = opt.findCoder(cls);
      if (coder != null) {
        return coder.encode(obj, type, ctx, target);
      }

      Object eqWrapper = opt.getEqualsWrapper(obj);

      //Prevent too many objects or cyclic reference.
      try {
        int p = ctx.objectPath.indexOf(eqWrapper);
        if (p >= 0) {
          return target.setValue(REF_PREFIX + StringUtil.appendRepeatedly(new StringBuilder(), "../", p + 1));
        }
      } catch(ClassCastException e) {
        // Workaround for some class that breaks equals() contract by doing caste before type check
        // We will ignore this error
      }

      if (ctx.convertedObjects.size() > MAX_OBJECTS || ctx.objectPath.size() > MAX_DEPTH)
        return target.setValue("[TRIMMED_DUE_TO_TOO_MANY_OBJECT]");

      TDNode orgResult = ctx.convertedObjects.get(eqWrapper);
      if(opt.dedupWithRef && orgResult != null) {
        String hash = (String) orgResult.getChildValue(HASH_KEY);
        if (hash == null) {  // This is the first reference. Only if there's a reference, the original map will display the hash value
          hash = Long.toHexString(abs(eqWrapper.hashCode()));
          orgResult.createChild(HASH_KEY).setValue(hash);
        }
        return target.setValue(REF_PREFIX + "#" + hash);
      }

      //Don't put empty collection and map as the hashCode method is not implemented properly
      if (! ((obj instanceof Collection) && ((Collection<?>)obj).isEmpty() ||
          (obj instanceof Map) && ((Map<?, ?>)obj).isEmpty())) {
        ctx.objectPath.push(eqWrapper);
      }

      if (cls.isArray()) {   //Handle it as any array
        CoderArray.getInstance().encode(obj, type, ctx, target);
      } else if (obj instanceof Collection) {  //Handle it as an Collection
        CoderCollection.getInstance().encode((Collection<?>)obj, type, ctx, target);
      } else if (obj instanceof Map) {
        CoderMap.getInstance().encode((Map<?,?>)obj, type, ctx, target);
      } else
        CoderObject.getInstance().encode(obj, type, ctx, target);

      if (target.getType() == TDNode.Type.MAP && target.hasChildren())
        ctx.convertedObjects.put(eqWrapper, target);

      return target;
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
   * @param jsonNode  The json object to decode, the o can be one of following type:
   * @param type  The target type to decode to
   * @param targetObj The target object to decode to, if it's null, a new Object will be created
   * @param ctx  The decode context
   * @return The decoded Object
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Object decode(TDNode jsonNode, Type type, Object targetObj, String name, BeanCoderContext ctx)
  {
    if(jsonNode == null)
      return null;
    Class<?> cls = ClassUtil.getGenericClass(type);

    int pathSize = ctx.objectPath.size();
    try{
      if (jsonNode.getValue() instanceof String && cls != String.class) {  // handle $ref type
        String s = (String) jsonNode.getValue();
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

      if (jsonNode.getType() == TDNode.Type.SIMPLE) {
        Object p = objectToSimpleType(jsonNode.getValue(), cls);
        if (p != null)
          return p;
      }

      ICoder<?> coder = ctx.option.findCoder(cls);
      if(coder != null)
        return coder.decode(jsonNode, type, targetObj, ctx);

      if (cls == String.class) {  // expect a string, but got non-string, just return serialized form of the the object
        return TDJSONWriter.getInstance().writeAsString(jsonNode);
      }

      if (cls.isArray())  // Handle the array type
        return CoderArray.getInstance().decode(jsonNode, type, targetObj, ctx);

      if (Collection.class.isAssignableFrom(cls))  // Handle the collection type
        return CoderCollection.getInstance().decode(jsonNode, type, targetObj, ctx);

      if (Map.class.isAssignableFrom(cls))
        return CoderMap.getInstance().decode(jsonNode, type, targetObj, ctx);

      //Handle bean type
      return CoderObject.getInstance().decode(jsonNode, type, targetObj, ctx);
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
