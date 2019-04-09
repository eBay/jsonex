package com.ebay.jsoncodercore.util;

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
