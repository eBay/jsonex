package org.jsonex.core.util;

import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * A two-way map which allow map values from key to value or reverse. This class provides default value if no match found
 * and support chained put method.
 */
public class TwoWayMap<K, V> {
  private final Map<K, V> map = new HashMap<>();
  private final Map<V, K> reverseMap = new HashMap<>();
  @Setter private K defaultKey;
  @Setter private V defaultValue;

  public static <K, V> TwoWayMap<K, V> of(K key, V value) { return new TwoWayMap<K, V>().put(key, value); }

  public TwoWayMap<K, V> put(K key, V value) {
    map.put(key, value);
    reverseMap.put(value, key);
    return this;
  }

  public V get(K key) { return key == null ? null : map.getOrDefault(key, defaultValue); }
  public K getKey(V value) { return value == null ? null : reverseMap.getOrDefault(value, defaultKey); }
}