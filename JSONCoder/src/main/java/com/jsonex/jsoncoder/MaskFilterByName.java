/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

import com.jsonex.core.util.BeanProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
@Setter @Getter @Accessors(chain=true) @RequiredArgsConstructor(staticName = "of")
public class MaskFilterByName implements FieldTransformer {
  private Map<String, MaskStrategy> fieldToStrategyMap = new HashMap<>();

  public MaskFilterByName add(String name, MaskStrategy strategy) {
    fieldToStrategyMap.put(name, strategy);
    return this;
  }

  @Override
  public FieldInfo apply(Object obj, BeanProperty property, BeanCoderContext beanCoderContext) {
    MaskStrategy strategy = fieldToStrategyMap.get(property.getName());
    if (strategy == null)
      return null;

    return new FieldInfo(property.getName(), String.class, strategy.apply(property.get(obj)));
  }
}
