package org.jsonex.core.util;

public class ArrayUtil {
  public static <T> int indexOf(T[] array, T e) {
    for (int i = 0; i < array.length; i++)
      if (array[i] == e)
        return i;
    return -1;
  }
}
