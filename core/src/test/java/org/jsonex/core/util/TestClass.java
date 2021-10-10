/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Map;

public class TestClass extends ArrayList<String> implements Comparable<TestClass> {
  public static class TestSubClass extends TestClass {
  }

  Map<String, Integer> stringIntMap;
  @Getter private static String privateStateField;
  @Getter private final String privateFinalField = null;
  @Override public int compareTo(TestClass o) { return 0; }
}
