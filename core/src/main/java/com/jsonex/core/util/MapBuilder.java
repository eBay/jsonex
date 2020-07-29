/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapBuilder<K, V> {
  @Getter final Map<K, V> map = new LinkedHashMap<>(); //NOPMD
  public MapBuilder<K, V> put(K key, V val) {
    map.put(key,  val);
    return this;
  }
  public MapBuilder() {}
  public MapBuilder(K key, V val) { put(key, val); }
}