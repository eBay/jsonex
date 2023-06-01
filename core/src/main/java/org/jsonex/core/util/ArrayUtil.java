package org.jsonex.core.util;

import org.jsonex.core.type.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayUtil {
  public static <T> int indexOf(@Nullable T[] array, T e) {
    if (array == null)
      return -1;
    for (int i = 0; i < array.length; i++)
      if (array[i] == e)
        return i;
    return -1;
  }

  public static <T> boolean contains(@Nullable T[] array, T e) {
    return indexOf(array, e) >= 0;
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

  public static <T> T[] subArray(@Nullable T[] array, int start, int length) {
    if (start < 0)
      start = array.length + start;
    return Arrays.copyOfRange(array, start, start + length);
  }

  public static <T> T[] subArray(@Nullable T[] array, int start) {
    return subArray(array, start, (array.length - start) % array.length);
  }

  public static <V> Optional<V> first(V[] source, Predicate<? super V> pred) {
    if (source != null)
      for (V s : source)
        if (pred.test(s))
          return Optional.of(s);
    return Optional.empty();
  }

  public static <V> int indexOf(V[] source, Predicate<? super V> pred) {
    if (source != null)
      for (int i = 0; i < source.length; i++)
        if (pred.test(source[i]))
          return i;
    return -1;
  }

  public static <V, T> T reduce(V[] source, T identity, BiFunction<T, V, T> accumulate) {
    T result = identity;
    if (source != null)
      for (V s : source)
        result = accumulate.apply(result, s);
    return result;
  }

  public static <V, T> T reduceTo(V[] source, T result, BiConsumer<T, V> accumulate) {
    if (source != null)
      for (V s : source)
        accumulate.accept(result, s);
    return result;
  }
}
