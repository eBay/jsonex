/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.jsoncoder.coder.CoderArray;
import com.jsonex.jsoncoder.coder.CoderCollection;
import com.jsonex.jsoncoder.coder.CoderMap;
import com.jsonex.jsoncoder.coder.CoderObject;
import com.jsonex.treedoc.TreeDoc;
import com.jsonex.treedoc.json.TDJSONWriter;
import com.jsonex.treedoc.TDNode;
import com.jsonex.core.util.ClassUtil;
import com.jsonex.core.util.StringUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static com.jsonex.core.util.ClassUtil.objectToSimpleType;

/**
 * A coder to convert java class to a TDNode
 *
 * <p>For an array or collection object, it will be encoded as TDNode of type ARRAY
 * <p>For Java Beans, it will be converted into TDNode of type MAP
 * <p>For some simple types such as Date, int, it will be converted TDNode of type SIMPLE
 *
 * <p>As this encoder won't store any type information, it will only infer the type from the class.
 * For collections fields, the element type information has to be specified with generic type. If not specified, the $type attribute
 * need to be provided. Otherwise
 */
@SuppressWarnings({"WeakerAccess"})
public class BeanCoder {
  public final static InjectableInstance<BeanCoder> it = InjectableInstance.of(BeanCoder.class);
  public static BeanCoder get() { return it.get(); }

  private final static int MAX_OBJECTS = 10_000;
  private final static int MAX_DEPTH = 50;
  public final static String ID_KEY = "$id";
  private final static String REF_KEY = "$ref";

  // Convenient utility method
  public TDNode encode(Object obj) { return encode(obj, new BeanCoderContext(JSONCoderOption.global), null); }
  public TDNode encode(Object obj, BeanCoderContext context, Type type) { return _encode(obj, context.reset(), type, new TreeDoc().getRoot()); }

  public Object decode(TDNode obj, Type type) { return decode(obj, type, null, "", new BeanCoderContext(JSONCoderOption.global)); }
  @SuppressWarnings("unchecked")
  public <T> T decode(TDNode obj, T target) {
    return (T)decode(obj, target.getClass(), target, "", new BeanCoderContext(JSONCoderOption.global));
  }

  @SuppressWarnings("unchecked")
  public <T> T deepClone(T src) { return src == null ? null : (T)decode(encode(src), src.getClass()); }
  public <T> T deepCopyTo(T src, T target) { return src == null ? null : decode(encode(src), target); }

  /**
   * This method should only be called internally during recursion, as it will not reset context
   */
  @SuppressWarnings("unchecked")
  TDNode _encode(Object obj, BeanCoderContext ctx, Type type, TDNode target)
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
          return setRef(target, StringUtil.appendRepeatedly(new StringBuilder(), "../", p + 1).toString());
        }
      } catch(ClassCastException e) {
        // Workaround for some class that breaks equals() contract by doing caste before type check
        // We will ignore this error
      }

      if (ctx.convertedObjects.size() > MAX_OBJECTS || ctx.objectPath.size() > MAX_DEPTH)
        return target.setValue("[TRIMMED_DUE_TO_TOO_MANY_OBJECT]");

      TDNode orgResult = ctx.convertedObjects.get(eqWrapper);
      if(opt.dedupWithRef && orgResult != null) {
        String hash = (String) orgResult.getChildValue(ID_KEY);
        if (hash == null) {  // This is the first reference. Only if there's a reference, the original map will display the hash value
          hash = Long.toHexString(abs(eqWrapper.hashCode()));
          orgResult.createChild(ID_KEY).setValue(hash);
        }
        return setRef(target, "#" + hash);
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

  private static TDNode setRef(TDNode node, String ref) {
    node.setType(TDNode.Type.MAP).createChild(REF_KEY).setValue(ref);
    return node;
  }

  /**
   *
   * @param tdNode  The json object to decode
   * @param type  The target type to decode to
   * @param targetObj The target object to decode to, if it's null, a new Object will be created
   * @param ctx  The decode context
   * @return The decoded Object
   */
  public Object decode(TDNode tdNode, Type type, Object targetObj, String name, BeanCoderContext ctx)
  {
    if(tdNode == null)
      return null;
    Class<?> cls = ClassUtil.getGenericClass(type);

    int pathSize = ctx.objectPath.size();
    try{
      Object refVal = tdNode.getChildValue(REF_KEY);
      if (refVal instanceof String) {
        String ref = (String)refVal;
        if(ref.length() > 0 && ref.charAt(0) == '#')  // Reference to a hashcode
          return ctx.hashToObjectMap.get(ref.substring(1));

        // Assume ref is in the format of "../../" etc.
        int level = ref.length() / 3;
        if (ctx.objectPath.size() < level)
          throw new BeanCoderException("Reference level exceeding objectPath:" + ctx.objectPath + "; ref:" + ref);
        return ctx.objectPath.get(level - 1);
      }

      if (tdNode.getType() == TDNode.Type.SIMPLE) {
        Object p = objectToSimpleType(tdNode.getValue(), cls);
        if (p != null)
          return p;
      }

      ICoder<?> coder = ctx.option.findCoder(cls);
      if(coder != null)
        return coder.decode(tdNode, type, targetObj, ctx);

      if (cls == String.class) {  // expect a string, but got non-string, just return serialized form of the the object
        return TDJSONWriter.get().writeAsString(tdNode);
      }

      if (cls.isArray())  // Handle the array type
        return CoderArray.getInstance().decode(tdNode, type, targetObj, ctx);

      if (Collection.class.isAssignableFrom(cls))  // Handle the collection type
        return CoderCollection.getInstance().decode(tdNode, type, targetObj, ctx);

      if (Map.class.isAssignableFrom(cls))
        return CoderMap.getInstance().decode(tdNode, type, targetObj, ctx);

      //Handle bean type
      return CoderObject.getInstance().decode(tdNode, type, targetObj, ctx);
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
