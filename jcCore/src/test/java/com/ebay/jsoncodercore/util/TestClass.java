package com.ebay.jsoncodercore.util;

import lombok.Getter;

public class TestClass {
  public static class TestSubClass extends TestClass {

  }

  @Getter private static String privateStateField;
  @Getter private final String privateFinalField = null;
}
