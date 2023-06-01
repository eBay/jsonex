/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.jsonex.core.type.Identifiable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.jsonex.core.util.LangUtil.safe;

/**
 * A collection of utilities related to Collection classes. Many methods here are better alternatives
 * to Java8 stream with more concise expression
 *
 * Regarding null input, for most of the list transformation methods, it will keep as silent as possible. That
 * means if you give a null, I'll give back you null without NPE. The principle is I just don't make it worse,
 * but won't complain it's already bad, I'm just a transformer but not a validator.
 */
public class ListUtil {
  /** Map with index. Have to use a different name as Java type inference has difficult to distinguish BiFuncion and Function */
  public static <C extends Collection<? super TDest>, TSrc, TDest> C mapWithIndex(
      Collection<? extends TSrc> source, BiFunction<? super TSrc, Integer, ? extends TDest> func, C dest) {
    if (source != null) {
      int idx = 0;
      for (TSrc src : source)
        dest.add(func.apply(src, idx++));
    }
    return dest;
  }

  public static <C extends Collection<? super TDest>, TSrc, TDest> C map(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func, C dest) {
    return mapWithIndex(source, (e, i) ->  func.apply(e), dest);
  }

  public static <TSrc, TDest> List<TDest> mapWithIndex(
      Collection<? extends TSrc> source, BiFunction<? super TSrc, Integer, ? extends TDest> func) {
    return source == null ? null : mapWithIndex(source, func, new ArrayList<>());
  }

  public static <TSrc, TDest> List<TDest> map(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func) {
    return source == null ? null : map(source, func, new ArrayList<>());
  }

  public static <C extends Collection<? super TDest>, TSrc, TDest> C flatMapWithIndex(Collection<? extends TSrc> source,
      BiFunction<? super TSrc, Integer, ? extends Collection< ? extends TDest>> func, C dest) {
    if (source != null) {
      int idx = 0;
      for (TSrc src : source) {
        Collection<? extends TDest> vals = func.apply(src, idx++);
        if (vals != null)
          dest.addAll(vals);
      }
    }
    return dest;
  }

  public static <C extends Collection<? super TDest>, TSrc, TDest> C flatMap(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends Collection< ? extends TDest>> func, C dest) {
    return flatMapWithIndex(source, (e, i) ->  func.apply(e), dest);
  }

  public static <TSrc, TDest> List<TDest> flatMapWithIndex(Collection<? extends TSrc> source,
      BiFunction<? super TSrc, Integer, ? extends Collection< ? extends TDest>> func) {
    return source == null ? null : flatMapWithIndex(source, func, new ArrayList<>());
  }

  public static <TSrc, TDest> List<TDest> flatMap(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends Collection< ? extends TDest>> func) {
    return source == null ? null : flatMap(source, func, new ArrayList<>());
  }

  public static <TSrc, TDest> Set<TDest> unique(
      Collection<? extends TSrc> source, Function<? super TSrc, ? extends TDest> func) {
    return source == null ? null : map(source, func, new HashSet<>());
  }

  public static <TId> List<TId> getIds(Collection<? extends Identifiable<TId>> identifiables) {
    return map(identifiables, param -> param.getId());
  }

  public static <K, E> Map<K, List<E>> groupBy(
      Collection<? extends E> source, Function<? super E, ? extends K> classifier) {
    if (source == null)
      return null;
    Map<K, List<E>> result = new LinkedHashMap<>();
    for (E e : source)
      result.computeIfAbsent(classifier.apply(e), key -> new ArrayList<>()).add(e);
    return result;
  }

  /**
   * convert a Map to another Map by applying keyFunc and valFunc to convert key and values. If converted key is null
   * the entry will be removed
   */
  public static <K, V, TK, TV> Map<TK, TV> map(Map<? extends K, ? extends V> source,
      Function<? super K, ? extends TK> keyFunc, Function<? super V, ? extends TV> valFunc) {
    if (source == null)
      return null;
    Map<TK, TV> result = new HashMap<>();
    for (Map.Entry<? extends K, ? extends V> entry : source.entrySet()) {
      TK key = safe(entry.getKey(), keyFunc);
      if (key != null)
        result.put(key, safe(entry.getValue(), valFunc));
    }
    return result;
  }

  public static <K, V, TV> Map<K, TV> mapValues(
      Map<? extends K, ? extends V> source, Function<? super V, ? extends TV> valFunc) {
    return map(source, Function.identity(), valFunc);
  }

  public static <K, V, TK> Map<TK, V> mapKeys(
      Map<? extends K, ? extends V> source, Function<? super K, ? extends TK> keyFunc) {
    return map(source, keyFunc, Function.identity());
  }

  /**
   * The list should contain Long values, otherwise ClassCastException will be thrown.
   */
  public static long[] toLongArray(Collection<Object> list) {
    if (list == null)
      return null;
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
    return toMapInto(source, keyFunc, valFunc, new LinkedHashMap<>());
  }

  // Have to use different name, as Java compile will confuse the overloaded methods with generics.
  public static <K, V, D extends Map<? super K, ? super V>> D toMapInto(
      Collection<V> source, Function<? super V, ? extends K> keyFunc, D dest) {
    return toMapInto(source, keyFunc, i -> i, dest);
  }

  public static <S, K, V, D extends Map<? super K, ? super V>> D toMapInto(
      Collection<S> source, Function<? super S, ? extends K> keyFunc, Function<? super S, ? extends V> valFunc, D dest) {
    if (source == null)
      return null;
    for (S s : source)
      dest.put(keyFunc.apply(s), valFunc.apply(s));
    return dest;
  }

  public static <V, S extends Collection<? extends V>, D extends Collection<? super V>> D filter(
      S source, Predicate<? super V> pred, D dest) {
    if (source != null)
      for (V s : source)
        if (pred.test(s))
          dest.add(s);
    return dest;
  }

  public static <V, C extends Collection<? extends V>> List<V> filter(C source, Predicate<? super V> pred) {
    return source == null ? null : filter(source, pred, new ArrayList<>());
  }

  public static <V, C extends Collection<? extends V>> List<V> filterNonNull(C source) {
    return filter(source, e -> e != null);
  }

  public static <V, C extends Collection<? extends V>> List<V> orderBy(
      C source, final Function<? super V, ? extends Comparable> by) {
    return orderBy(source, by, false);
  }

  public static <V, C extends Collection<? extends V>, K extends Comparable<K>> List<V> orderBy(
      C src, final Function<? super V, K> by, final boolean desc) {
    if (src == null)
      return null;
    List<V> dest = new ArrayList<>(src.size());
    dest.addAll(src);  // clone
    Collections.sort(dest, (o1, o2) -> {
      K v1 = by.apply(o1);
      K v2 = by.apply(o2);
      int result;
      if (v1 == null)
        result = v2 == null ? 0 : -1;
      else
        result = v2 == null ? 1 : v1.compareTo(v2);
      return desc ? - result : result;
    });
    return dest;
  }

  public static boolean inLongs(long match, long... longs) {
    if (longs != null)
      for (long l : longs)
        if (match == l)
          return true;
    return false;
  }

  public static <T> boolean isIn(T match, T... values) {
    if (values != null)
      for (T l : values) {
        if (match == null) {
          if (l == null)
            return true;
          continue;
        }
        if (match.equals(l))
          return true;
      }
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

  public static <T> String join(T[] list, char delimiter) { return join(Arrays.asList(list), delimiter); }
  public static <T> String join(Collection<T> list, char delimiter) {
    StringBuilder sb = new StringBuilder();
    for(Object obj : list) {
      if(sb.length() > 0)
        sb.append(delimiter);
      sb.append(obj);
    }
    return sb.toString();
  }

  public static <V, C extends Collection<V>> boolean exists(C source, Predicate<? super V> pred) {
    return source == null ? false : first(source, pred).isPresent();
  }

  public static <V, C extends Collection<V>> Optional<V> first(C source, Predicate<? super V> pred) {
    if (source != null)
      for (V s : source)
        if (pred.test(s))
          return Optional.of(s);
    return Optional.empty();
  }

  public static <V, C extends List<V>> int indexOf(C source, Predicate<? super V> pred) {
    if (source != null)
      for (int i = 0; i < source.size(); i++)
        if (pred.test(source.get(i)))
          return i;
    return -1;
  }

  public static <V, S extends Collection<? extends V>, D extends Collection<? super V>> D takeBetween(
      S source, Predicate<? super V> dropPred, Predicate<? super V> whilePred, D dest) {
    if (source != null) {
      boolean startTaking = false;
      for (V s : source) {
        if (!startTaking && dropPred.test(s))
          continue;
        startTaking = true;
        if (!whilePred.test(s))
          break;
        dest.add(s);
      }
    }
    return dest;
  }

  /** This is a combination of dropWhile and takeWhile */
  public static <V, S extends Collection<? extends V>> List<V> takeBetween(
      S source, Predicate<? super V> dropPred, Predicate<? super V> whilePred) {
    return source == null ? null : takeBetween(source, dropPred, whilePred, new ArrayList<>());
  }

  public static <V, S extends Collection<? extends V>, D extends Collection<? super V>> D takeWhile(
      S source, Predicate<? super V> pred, D dest) {
    return takeBetween(source, (v) -> false, pred, dest);
  }

  public static <V, S extends Collection<? extends V>> List<V> takeWhile(S source, Predicate<? super V> pred) {
    return source == null ? null : takeWhile(source, pred, new ArrayList<>());
  }

  public static <V, S extends Collection<? extends V>, D extends Collection<? super V>> D dropWhile(
      S source, Predicate<? super V> pred, D dest) {
    return takeBetween(source, pred, v -> true, dest);
  }

  public static <V, S extends Collection<? extends V>> List<V> dropWhile(S source, Predicate<? super V> pred) {
    return dropWhile(source, pred, new ArrayList<>());
  }

  public static <T> Optional<T> last(List<T> list) {
    if (list == null)
      return Optional.empty();
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
  }

  public static <T> Optional<T> first(Collection<T> list) {
    if (list == null)
      return Optional.empty();
    return list.isEmpty() ? Optional.empty() : Optional.of(list.iterator().next());
  }

  public static <T> boolean containsAny(Collection<? super T> list, Collection<? extends T> elements) {
    if (list != null)
      for (T e : elements)
        if (list.contains(e))
          return true;
    return false;
  }

  public static <T> boolean containsAny(Collection<? super T> list, T... elements) {
    if (list != null)
      for (T e : elements)
        if (list.contains(e))
          return true;
    return false;
  }

  public static void removeLast(List<?> list) { list.remove(list.size() - 1); }

  /** Set at a particular location. It will auto resize the list if necessary */
  public static <T, L extends List<? super T>> L setAt(L list, int idx, T value) {
    if (idx < list.size()) {
      list.set(idx, value);
      return list;
    }
    for (int i = list.size(); i < idx; i++)
      list.add(null);
    list.add(value);
    return list;
  }

  public static <T> T getOrDefault(List<T> list, int idx, T value) {
    return idx < list.size() ? list.get(idx) : value;
  }

  public static <T, L extends List<T>> L mutateAt(L list, int idx, T defaultVal, Function<? super T, ? extends T> mutator) {
    return setAt(list, idx, mutator.apply(getOrDefault(list, idx, defaultVal)));
  }

  /** build a copy of mutable Set whose content will be independent with original array once created */
  public static <T> Set<T> setOf(T... e) { return e == null ? null : new LinkedHashSet<>(Arrays.asList(e)); }

  /** build a copy of mutable list whose content will be independent with original array once created */
  public static <T> List<T> listOf(T... e) { return e == null ? null : new ArrayList<>(Arrays.asList(e)); }

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

  public static <V, T> T reduce(List<V> source, T identity, BiFunction<T, V, T> accumulate) {
    T result = identity;
    if (source != null)
      for (V s : source)
        result = accumulate.apply(result, s);
    return result;
  }

  public static <V, T> T reduceTo(List<V> source, T result, BiConsumer<T, V> accumulate) {
    if (source != null)
      for (V s : source)
        accumulate.accept(result, s);
    return result;
  }
}
