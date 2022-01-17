package org.jsonex.core.util;

import org.jsonex.core.type.Nullable;

import java.lang.reflect.Array;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ArrayUtil {
  public static <T> int indexOf(@Nullable T[] array, T e) {
    if (array == null)
      return -1;
    for (int i = 0; i < array.length; i++)
      if (array[i] == e)
        return i;
    return -1;
  }

  public static <TSrc, TDest> TDest[] map(@Nullable TSrc[] source, Function<? super TSrc, ? extends TDest> func, TDest[] dest) {
    return mapWithIndex(source, (s, idx) -> func.apply(s), dest);
  }

  public static <TSrc, TDest> TDest[] mapWithIndex(
      @Nullable TSrc[] source, BiFunction<? super TSrc, Integer, ? extends TDest> func, TDest[] dest) {
    if (source == null)
      return null;
    if (dest.length < source.length)
      dest = (TDest[]) Array.newInstance(dest.getClass().getComponentType(), source.length);
    for (int i = 0; i < source.length; i++)
      dest[i] = func.apply(source[i], i);
    return dest;
  }

  public static Integer[] box(@Nullable int[] ints) {
    if (ints == null)
      return null;
    Integer[] result = new Integer[ints.length];
    for (int i = 0; i < ints.length; i++)
      result[i] = ints[i];
    return result;
  }

  public static int[] unbox(@Nullable Integer[] ints) {
    if (ints == null)
      return null;
    int[] result = new int[ints.length];
    for (int i = 0; i < ints.length; i++)
      result[i] = ints[i];
    return result;
  }
}
