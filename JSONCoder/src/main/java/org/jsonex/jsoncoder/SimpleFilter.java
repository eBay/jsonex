/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

import org.jsonex.core.util.BeanProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter @Getter @Accessors(chain=true) @RequiredArgsConstructor(staticName = "of")
public class SimpleFilter implements FieldTransformer {
  @Getter final boolean include;  // Default is excluded
  private Set<String> properties = new HashSet<>();

  public static SimpleFilter of() { return of(false); }
  public static SimpleFilter exclude( String... props) {
    return of().addProperties(props);
  }

  public static SimpleFilter include(String... props) {
    return of(true).addProperties(props);
  }

  private boolean isFieldSkipped(String field) {
    return isInclude() ? !properties.contains(field) : properties.contains(field);
  }
  
  public SimpleFilter addProperties(String... props) {
    properties.addAll(Arrays.asList(props));
    return this;
  }
  
  public SimpleFilter removeProperties(String... props) {
    properties.removeAll(Arrays.asList(props));
    return this;
  }

  @Override
  public FieldInfo apply(Object o, BeanProperty property, BeanCoderContext beanCoderContext) {
    if(isFieldSkipped(property.getName()))
      return new FieldInfo(null, null, null);
    return null;
  }
}