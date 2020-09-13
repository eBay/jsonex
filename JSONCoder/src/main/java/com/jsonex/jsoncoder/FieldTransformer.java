/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.core.type.Func;
import com.jsonex.core.util.BeanProperty;
import com.jsonex.jsoncoder.FieldTransformer.FieldInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

public interface FieldTransformer extends Func._3<Object, BeanProperty, BeanCoderContext, FieldInfo> {
  InjectableInstance<FieldTransformer> it = InjectableInstance.of(DefaultImpl::new);

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
}
