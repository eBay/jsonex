/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.coder;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.BeanCoderException;
import org.jsonex.jsoncoder.ICoder;
import org.jsonex.treedoc.TDNode;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import static org.jsonex.core.util.StringUtil.toTrimmedStr;

public class CoderCollection implements ICoder<Collection> {
  public static final InjectableInstance<CoderCollection> it = InjectableInstance.of(CoderCollection.class);
  public static CoderCollection get() { return it.get(); }

  @Override public Class<Collection> getType() { return Collection.class; }

  @Override public TDNode encode(Collection obj, Type type, BeanCoderContext ctx, TDNode target) {
    target.setType(TDNode.Type.ARRAY);
    if (ctx.getOption().isStrictOrder()
        && obj instanceof Set && !(obj instanceof SortedSet || obj instanceof LinkedHashSet || obj instanceof EnumSet)) {
      Set set = new TreeSet(FullbackComparator.it); // Due to instability of Set iteration order, we copy it to TreeSet to make iteration stable
      set.addAll(obj);
      obj = set;
    }

    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);
    Type childType = null;
    if (actualTypeParameters != null)
      childType = actualTypeParameters[0];

    try {
      int i = 0;
      for (Object o1 : (Collection<?>) obj) {
        ctx.encode(o1, childType, target.createChild(null));
        if (i++ > ctx.getOption().getMaxElementsPerNode())
          break;
      }
    } catch(ConcurrentModificationException e) {
      // Ignore, some collection will be changed during serialization, such as class loader
    }

    return target;
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  @Override public Collection decode(TDNode tdNode, Type type, Object targetObj, BeanCoderContext ctx) {
    if (tdNode.getType() != TDNode.Type.ARRAY)
      throw new BeanCoderException("Incorrect input, the input has to be an array:" + toTrimmedStr(tdNode, 500));

    Class<?> cls = ClassUtil.getGenericClass(type);

    Type[] actualTypeParameters = ClassUtil.getGenericTypeActualParams(type);
    if (actualTypeParameters == null)
      throw new BeanCoderException("For collection type, you have to specify the actual type: " + cls);
    Type childType = actualTypeParameters[0];

    Collection<Object> result = (Collection<Object>) targetObj;
    if (result == null) {
      int modifier = cls.getModifiers();
      if (Modifier.isAbstract(modifier) ||
          Modifier.isInterface(modifier)) {
        //Use the default implementation ArrayList
        if (EnumSet.class.isAssignableFrom(cls))
          result = (EnumSet) EnumSet.noneOf((Class<Enum>) childType);
        else if (Set.class.isAssignableFrom(cls))
          result = new HashSet<>();
        else
          result = new ArrayList<>();
      } else
        result = (Collection<Object>) cls.newInstance();
    } else {
      if (!ctx.getOption().isMergeArray())
        result.clear();
    }

    ctx.getNodeToObjectMap().put(tdNode, result);

    for (int i = 0; i < tdNode.getChildrenSize(); i++)
      result.add(ctx.decode(tdNode.getChildren().get(i), childType, null, Integer.toString(i)));
    return result;
  }
}