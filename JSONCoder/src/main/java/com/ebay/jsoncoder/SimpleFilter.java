/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@SuppressWarnings("UnusedReturnValue")
@Setter @Getter @Accessors(chain=true)
public class SimpleFilter implements IFilter {
  @Getter private final Class<?> type;
  @Getter @Setter boolean include;  // Default is excluded
  private Set<String> properties = new HashSet<>();

  private SimpleFilter(Class<?> type) { this.type = type; }
  public static SimpleFilter of(Class<?> type) { return new SimpleFilter(type); }

  @Override
  public Boolean isFieldSkipped(String field) {
    if (properties.contains(field))
      return !include;   // If explicitly defined in properties, no further checks
    
    if (include && !properties.contains(field))
      return true;
    
    return null;
  }
  
  public SimpleFilter addProperties(String... props) {
    properties.addAll(Arrays.asList(props));
    return this;
  }
  
  public SimpleFilter removeProperties(String... props) {
    properties.removeAll(Arrays.asList(props));
    return this;
  }
}