/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.fieldTransformer;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.type.Func;
import org.jsonex.core.util.BeanProperty;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.fieldTransformer.FieldTransformer.FieldInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

public interface FieldTransformer extends Func._3<Object, BeanProperty, BeanCoderContext, FieldInfo> {
  InjectableInstance<FieldTransformer> defaultImpl = InjectableInstance.of(DefaultImpl::new);
  static FieldTransformer ofDefault() { return defaultImpl.get(); }

  @Data @Accessors(chain = true) @AllArgsConstructor
  class FieldInfo {
    String name;
    Type type;
    Object obj;
  }
  /**
   * Return null, means no transformation, the caller will continue to call next filter. Otherwise, will stop
   */
  @Override FieldInfo apply(Object o, BeanProperty property, BeanCoderContext beanCoderContext);

  class DefaultImpl implements FieldTransformer {
    @Override public FieldInfo apply(Object o, BeanProperty property, BeanCoderContext beanCoderContext) {
      return new FieldInfo(property.getName(), property.getGenericType(), property.get(o));
    }
  }

  // Factory methods
  static SimpleFilter exclude(String... props) { return SimpleFilter.of().addProperties(props); }
  static SimpleFilter include(String... props) { return SimpleFilter.of(true).addProperties(props); }

  static MaskFilterByName mask(String... props) {
    MaskFilterByName filter = MaskFilterByName.of();
    for (String p : props)
      filter.add(p, MaskStrategy.ofDefault());
    return filter;
  }

  static MaskFilterByName mask(MaskStrategy strategy, String... props) {
    MaskFilterByName filter = MaskFilterByName.of();
    for (String p : props)
      filter.add(p, strategy);
    return filter;
  }
}
