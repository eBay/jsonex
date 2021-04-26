/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.fieldTransformer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.fieldTransformer.FieldTransformer.FieldInfo;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

public interface FieldTransformer extends BiFunction<FieldInfo, BeanCoderContext, FieldInfo> {
  @Data @Accessors(chain = true) @AllArgsConstructor
  class FieldInfo {
    String name;
    Type type;
    Object obj;
  }

  default boolean shouldInclude(String name, BeanCoderContext ctx) { return true; }
  default FieldInfo apply(FieldInfo fieldInfo, BeanCoderContext beanCoderContext) { return fieldInfo; }

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
