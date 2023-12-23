package org.jsonex.core.util;

import static org.jsonex.core.util.ListUtil.listOf;
import static org.jsonex.core.util.SetUtil.setOf;
import static org.jsonex.core.util.SetUtil.difference;
import static org.jsonex.core.util.SetUtil.intersection;
import static org.jsonex.core.util.SetUtil.symmetricDifference;
import static org.jsonex.core.util.SetUtil.union;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Set;

public class SetUtilTest {
  private static Set<Integer> SET1 = setOf(1,2,3);
  private static Set<Integer> SET2 = setOf(2,3,4);

  @Test public void testSetOf() {
    assertTrue("set should contain all the elements", setOf(1,2,3).containsAll(listOf(1,2,3))); }

  @Test public void testUnion() {
    assertEquals(union(SET1, SET2), setOf(1,2,3,4));
  }

  @Test public void testDifference() {
    assertEquals(difference(SET1, SET2), setOf(1));
  }

  @Test public void testIntersection() {
    assertEquals(intersection(SET1, SET2), setOf(2, 3));
  }

  @Test public void testSymmetricDifference() {
    assertEquals(symmetricDifference(SET1, SET2), setOf(1, 4));
  }
}
