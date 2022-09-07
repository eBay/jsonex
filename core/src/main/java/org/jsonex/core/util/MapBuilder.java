/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple wrapper of Map to provide chainable put() method to support fluent coding style. This class is more of a wrapper
 * instead of Builder
 */
public class MapBuilder<K, V> {
  @Getter final Map<K, V> map = new LinkedHashMap<>();
  public MapBuilder<K, V> put(K key, V val) {
    map.put(key,  val);
    return this;
  }
  public MapBuilder() {}
  public MapBuilder(K key, V val) { put(key, val); }
  public Map<K, V> build() { return map; }

  @Deprecated // Use of instead
  public static <K, V> MapBuilder<K, V> mapOf(K key, V val) {
    return new MapBuilder<>(key, val);
  }
  public static <K, V> MapBuilder<K, V> of(K key, V val) {
    return new MapBuilder<>(key, val);
  }
}