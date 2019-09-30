package com.jsonex.treedoc;

public class ArrayCharSourceTest extends BaseCharSourceTest {
  protected CharSource createCharSource(String str, int startIndex, int endIndex) {
    return new ArrayCharSource(str.toCharArray(), startIndex, endIndex);
  }
}
