/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.type;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TypeRefTest {
  @Test
  public void testTypes() {
    assertGenericTypes(TypeRef.LIST_OF_STRING, List.class, String.class);
    assertGenericTypes(TypeRef.LIST_OF_INTEGER, List.class, Integer.class);
    assertGenericTypes(TypeRef.LIST_OF_LONG, List.class, Long.class);
    assertGenericTypes(TypeRef.SET_OF_STRING, Set.class, String.class);
    assertGenericTypes(TypeRef.SET_OF_INTEGER, Set.class, Integer.class);
    assertGenericTypes(TypeRef.SET_OF_LONG, Set.class, Long.class);
  }
  
  private static void assertGenericTypes(Type type, Class<?> rowType, Class<?>... arguments) {
    ParameterizedType pt = (ParameterizedType) type;
    assertEquals(rowType, pt.getRawType());
    assertArrayEquals(arguments, pt.getActualTypeArguments());
  }
}
