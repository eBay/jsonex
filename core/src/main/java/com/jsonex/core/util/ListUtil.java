/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.jsonex.core.type.Identifiable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A collection of utilities related to Collection classes. Many methods here are a better alternative
 * to Java8 stream with more concise expression
 */
public class ListUtil {
  public static <C extends Collection<? super TDest>, TSrc, TDest> C map(
      Collection<? extends TSrc> source, BiFunction<? super TSrc, Integer, ? extends TDest> func, C dest) {
    if (source == null)
      return null;
    int idx = 0;
    for (TSrc src : source)
      dest.add(func.apply(src, idx++));
    return dest;
  }

  public static <C extends Collection<? super TDest>, TSrc, TDest> C map(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func, C dest) {
    return map(source, (e, i) ->  func.apply(e), dest);
  }

  public static <TSrc, TDest> List<TDest> map(
      Collection<? extends TSrc> source, BiFunction<? super TSrc, Integer, ? extends TDest> func) {
    return map(source, func, new ArrayList<>());
  }

  public static <TSrc, TDest> List<TDest> map(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func) {
    return map(source, func, new ArrayList<>());
  }

  public static <TId> List<TId> getIds(Collection<? extends Identifiable<TId>> identifiables) {
    return map(identifiables, param -> param.getId());
  }

  public static <K, E> Map<K, List<E>> groupBy(
      Collection<? extends E> source, Function<? super E, ? extends K> classifier) {
    Map<K, List<E>> result = new LinkedHashMap<>();
    for (E e : source) {
      K k = classifier.apply(e);
      List<E> v = result.get(k);
      if (v == null) {
        v = new ArrayList<>();
        result.put(k, v);
      }
      v.add(e);
    }
    return result;
  }

  public static <K, V, T> Map<K, T> mapValues(
      Map<? extends K, ? extends V> source, Function<? super V, ? extends T> func) {
    Map<K, T> result = new HashMap<>();
    for (K k : source.keySet())
      result.put(k, func.apply(source.get(k)));
    return result;
  }

  /**
   * The list should contain Long values. Otherwise ClassCastException will be thrown.
   */
  public static long[] toLongArray(Collection<Object> list) {
    long[] result = new long[list.size()];
    int i = 0;
    for (Object e : list) {
      long l;
      if (e == null)
        l = 0;
      else if (e instanceof String)
        l = Long.parseLong((String)e);
      else if (e instanceof Number)
        l = ((Number)e).longValue();
      else
        throw new IllegalArgumentException("Expect list of String or Numbers, actual got: " + e.getClass() + ", e=" + e);
      result[i++] = l;
    }
    return result;
  }

  public static <K, V> Map<K, V> toMap(Collection<V> source, Function<? super V, ? extends K> keyFunc) {
    return toMap(source, keyFunc, Function.identity());
  }

  public static <S, K, V> Map<K, V> toMap(
      Collection<S> source, Function<? super S, ? extends K> keyFunc, Function<? super S, ? extends V> valFunc) {
    Map<K, V> map = new LinkedHashMap<>();
    for (S s : source)
      map.put(keyFunc.apply(s), valFunc.apply(s));
    return map;
  }

  public static <V, S extends Collection<? extends V>, D extends Collection<? super V>> D filter(
      S source, Predicate<? super V> pred, D dest) {
    for (V s : source) {
      if (pred.test(s))
        dest.add(s);
    }
    return dest;
  }

  public static <V, C extends Collection<? extends V>> List<V> filter(C source, Predicate<? super V> pred) {
    return filter(source, pred, new ArrayList<>());
  }

  public static <V, C extends Collection<? extends V>> List<V> orderBy(
      C source, final Function<? super V, ? extends Comparable> by) {
    return orderBy(source, by, false);
  }

  public static <V, C extends Collection<? extends V>, K extends Comparable<K>> List<V> orderBy(
      C src, final Function<? super V, K> by, final boolean desc) {
    List<V> dest = new ArrayList<>(src.size());
    dest.addAll(src);  // clone
    Collections.sort(dest, (o1, o2) -> {
      K v1 = by.apply(o1);
      K v2 = by.apply(o2);
      int result;
      if (v1 == null)
        result = v2 == null ? 0 : -1;
      else {
        result = v2 == null ? 1 : v1.compareTo(v2);
      }
      return desc ? - result : result;
    });
    return dest;
  }

  public static boolean contains(long[] longs, long match) {
    if (longs != null)
      for (long l : longs)
        if (match == l)
          return true;
    return false;
  }

  public static <T> String join(T[] list, String delimiter) { return join(Arrays.asList(list), delimiter); }
  public static <T> String join(Collection<T> list, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for(Object obj : list) {
      if(sb.length() > 0)
        sb.append(delimiter);
      sb.append(obj);
    }
    return sb.toString();
  }

  public static <V, C extends Collection<V>> boolean exists(C source, Predicate<? super V> pred) {
    return first(source, pred) != null;
  }

  public static <V, C extends Collection<V>> V first(C source, Predicate<? super V> pred) {
    if (source != null)
      for (V s : source) {
        if (pred.test(s))
          return s;
      }
    return null;
  }

  public static <V, C extends List<V>> int indexOf(C source, Predicate<? super V> pred) {
    if (source != null)
      for (int i = 0; i < source.size(); i++) {
        if (pred.test(source.get(i)))
          return i;
      }
    return -1;
  }

  public static <V, S extends Collection<? extends V>, D extends Collection<? super V>> D takeWhile(
      S source, Predicate<? super V> pred, D dest) {
    if (source != null)
      for (V s : source) {
        if (!pred.test(s))
          break;
        dest.add(s);
      }
    return dest;
  }

  public static <V, S extends Collection<? extends V>> List<V> takeWhile(S source, Predicate<? super V> pred) {
    return takeWhile(source, pred, new ArrayList<>());
  }

  public static <T> T last(List<T> list) { return list == null ? null : list.get(list.size() - 1); }
  public static <T> T first(Collection<T> list) {
    return list == null || list.size() == 0 ? null : list.iterator().next();
  }

  public static <T> boolean containsAny(Collection<T> list, T... elements) {
    if (list != null)
      for (T e : elements) {
        if (list.contains(e))
          return true;
      }
    return false;
  }

  public static void removeLast(List<?> list) { list.remove(list.size() - 1); }

  public static <T> Set<T> setOf(T... e) { return new LinkedHashSet<>(Arrays.asList(e)); }

  public static <K, V, M extends Map<K, V>> M mergeWith(M target, Map<? extends K, ? extends V> source,
      BiFunction<? super V, ? super V, ? extends V> mergeFunc) {
    source.forEach((k, v) -> target.merge(k, v, mergeFunc));
    return target;
  }

  public static <K, V, L extends Collection<V>, M extends Map<K, L>> M mergeWith(
      M target, Map<? extends K, ? extends L> source) {
    return mergeWith(target, source, (l1, l2) -> {
      l1.addAll(l2);
      return l1;
    });
  }
}
