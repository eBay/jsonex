package org.jsonex.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TwoWayMapTest {
  @Test public void testGet() {
    TwoWayMap map = TwoWayMap.of("a", "A").put("b", "B").setDefaultKey("+").setDefaultValue("-");
    assertEquals("A", map.get("a"));
    assertEquals("b", map.getKey("B"));
    assertNull(map.getKey(null));
    assertEquals("+", map.getKey("C"));
    assertEquals("-", map.get("c"));
  }
}
