package org.jsonex.core.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilTest {
  @Test public void testMap() {
    assertArrayEquals(new String[]{"1", "2", "3"}, ArrayUtil.map(new Integer[]{ 1, 2, 3 }, i -> i + "", new String[0]));
  }

  @Test public void testBox() {
    assertArrayEquals(new Integer[]{1, 2, 3}, ArrayUtil.box(new int[]{ 1, 2, 3 }));
    assertArrayEquals(new int[]{1, 2, 3}, ArrayUtil.unbox(new Integer[]{ 1, 2, 3 }));
  }

}
