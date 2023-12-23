package org.jsonex.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Java building set functions didn't provide functional API that operation on set without side effect.
 * This util class provide functional API for standard set operation without won't mutate the input set.
 */
public class SetUtil {
  /** build a copy of mutable Set whose content will be independent with original array once created */
  public static <T> Set<T> setOf(T... e) { return e == null ? null : new LinkedHashSet<>(Arrays.asList(e)); }

  public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
    Set<T> result = new HashSet<>(set1);
    result.addAll(set2);
    return result;
  }

  public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
    Set<T> result = new HashSet<>(set1);
    result.removeAll(set2);
    return result;
  }

  public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
    Set<T> result = new HashSet<>(set1);
    result.retainAll(set2);
    return result;
  }

  public static <T> Set<T> symmetricDifference(Set<T> set1, Set<T> set2) {
    Set<T> result = new HashSet<>(set1);
    result.addAll(set2);
    Set<T> tmp = new HashSet<>(set1);
    tmp.retainAll(set2);
    result.removeAll(tmp);
    return result;
  }
}
