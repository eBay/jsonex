/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder.fieldTransformer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jsonex.jsoncoder.BeanCoderContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter @Getter @Accessors(chain=true) @RequiredArgsConstructor(staticName = "of")
public class SimpleFilter implements FieldTransformer {
  @Getter final boolean include;  // Default is excluded
  private Set<String> properties = new HashSet<>();

  public static SimpleFilter of() { return of(false); }

  public SimpleFilter addProperties(String... props) {
    properties.addAll(Arrays.asList(props));
    return this;
  }
  
  public SimpleFilter removeProperties(String... props) {
    properties.removeAll(Arrays.asList(props));
    return this;
  }

  @Override
  public boolean shouldInclude(String name, BeanCoderContext beanCoderContext) {
    return isInclude() ? properties.contains(name) : !properties.contains(name);
  }
}