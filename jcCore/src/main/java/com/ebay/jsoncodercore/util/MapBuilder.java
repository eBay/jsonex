/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

public class MapBuilder<K, V> {
  @Getter final Map<K, V> map = new LinkedHashMap<>();//NOPMD
  public MapBuilder<K, V> put(K key, V value) {
    map.put(key,  value);
    return this;
  }
}