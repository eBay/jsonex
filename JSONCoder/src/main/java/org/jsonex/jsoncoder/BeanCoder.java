/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.core.util.StringUtil;
import org.jsonex.jsoncoder.coder.CoderArray;
import org.jsonex.jsoncoder.coder.CoderCollection;
import org.jsonex.jsoncoder.coder.CoderMap;
import org.jsonex.jsoncoder.coder.CoderObject;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.TreeDoc;
import org.jsonex.treedoc.json.TDJSONWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static org.jsonex.core.util.ClassUtil.objectToSimpleType;

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
          return setRef(target, StringUtil.appendRepeatedly(new StringBuilder(), p + 1, "../").toString());
        }
      } catch(ClassCastException | IllegalArgumentException e) {
        // Workaround for some class that breaks equals() contract by doing caste before type check, or asset the type
        // We will ignore this error
      }

      if (ctx.objectCount++ > opt.maxObjects || ctx.objectPath.size() > opt.maxDepth)
        return target.setValue("[TRIMMED_DUE_TO_TOO_MANY_OBJECT]");

      TDNode orgResult = ctx.objToNodeMap.get(eqWrapper);
      if(opt.dedupWithRef && orgResult != null) {
        if (orgResult.getType() == TDNode.Type.MAP) {
          Integer id = (Integer) orgResult.getChildValue(TDNode.ID_KEY);
          if (id == null) {  // This is the first reference. Only if there's a reference, the original map will display the hash value
            id = ctx.getNextId();
            orgResult.createChild(TDNode.ID_KEY).setValue(id);
          }
          return setRef(target, "#" + id);
        } else  // It's Array
          return setRef(target, orgResult.getPathAsString());
      }

      //Don't put empty collection and map as the hashCode method is not implemented properly
      if (! ((obj instanceof Collection) && ((Collection<?>)obj).isEmpty() ||
          (obj instanceof Map) && ((Map<?, ?>)obj).isEmpty())) {
        ctx.objectPath.push(eqWrapper);
      }

      if (cls.isArray()) {   //Handle it as any array
        CoderArray.get().encode(obj, type, ctx, target);
      } else if (obj instanceof Collection) {  //Handle it as an Collection
        CoderCollection.get().encode((Collection<?>)obj, type, ctx, target);
      } else if (obj instanceof Map) {
        CoderMap.get().encode((Map<?,?>)obj, type, ctx, target);
      } else
        CoderObject.get().encode(obj, type, ctx, target);

      if (target.getType() == TDNode.Type.MAP || target.getType() == TDNode.Type.ARRAY)
        ctx.objToNodeMap.put(eqWrapper, target);

      return target;
    } catch (Throwable ex) {
      throw new BeanCoderException(ex);
    } finally{
      if(ctx.objectPath.size() > pathSize) {
        ctx.objectPath.pop();
      }
    }
  }

  private static TDNode setRef(TDNode node, String ref) {
    node.setType(TDNode.Type.MAP).createChild(TDNode.REF_KEY).setValue(ref);
    return node;
  }

  /**
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

    try{
      Object refVal = tdNode.getChildValue(TDNode.REF_KEY);
      if (refVal instanceof String) {
        String ref = (String)refVal;
        TDNode target = tdNode.getByPath(ref);
        if (target == null)
          throw new BeanCoderException("Reference is not found: ref:" + ref + "; current Node:" + tdNode.getPathAsString());
        return ctx.nodeToObjectMap.get(target);
      }

      if (tdNode.getType() == TDNode.Type.SIMPLE) {
        Object result = objectToSimpleType(tdNode.getValue(), cls);
        if (result != null)
          return result;
      }

      ICoder<?> coder = ctx.option.findCoder(cls);
      if(coder != null)
        return coder.decode(tdNode, type, targetObj, ctx);

      if (cls == String.class) // expect a string, but got non-string, just return serialized form of the the object
        return TDJSONWriter.get().writeAsString(tdNode);

      if (cls.isArray())  // Handle the array type
        return CoderArray.get().decode(tdNode, type, targetObj, ctx);

      if (Collection.class.isAssignableFrom(cls))  // Handle the collection type
        return CoderCollection.get().decode(tdNode, type, targetObj, ctx);

      if (Map.class.isAssignableFrom(cls))
        return CoderMap.get().decode(tdNode, type, targetObj, ctx);

      //Handle bean type
      return CoderObject.get().decode(tdNode, type, targetObj, ctx);
    } catch(Throwable e) {
      throw new BeanCoderException("failed to decode:"+type + "; name=" + name, e);
    }
  }

  //Create this method just to avoid findbug issues.
  private static int abs(int a) { return a < 0 ? -a : a; }
}
