/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapBuilderTest {
  @Test public void testBuildBuilder() {
    Map<String, Integer> map = new MapBuilder<String, Integer>()
        .put("key1", 1)
        .put("key2", 2)
        .getMap();
    assertEquals((Integer)1, map.get("key1"));
    assertEquals((Integer)2, map.get("key2"));
  }
}
