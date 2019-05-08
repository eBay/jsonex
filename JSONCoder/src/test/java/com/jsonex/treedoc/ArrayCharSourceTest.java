package com.jsonex.treedoc;

public class ArrayCharSourceTest extends BasecharSourceTest {
  protected CharSource createCharSource(String str, int startIndex, int endIndex) {
    return new ArrayCharSource(str.toCharArray(), startIndex, endIndex);
  }
}
