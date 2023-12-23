package org.jsonex.core.util;

import org.jsonex.core.util.ListUtilTest.TestCls;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.jsonex.core.type.Operator.eq;
import static org.jsonex.core.util.ListUtilTest.TestCls.F_TYPE;
import static org.junit.Assert.*;

public class ArrayUtilTest {
  private TestCls[] buildArray() {
    return new TestCls[]{
        new TestCls(0, null, 0, Arrays.asList("a", "b")),
        new TestCls(1, "name1", 1, Arrays.asList("c", "d", "e")),
        new TestCls(2, null, 2, Arrays.asList()),
        new TestCls(3, "name3", 2, null),
    };
  }

  @Test public void testMap() {
    assertArrayEquals(new String[]{"1", "2", "3"}, ArrayUtil.map(new Integer[]{ 1, 2, 3 }, i -> i + "", new String[0]));
  }

  @Test public void testBox() {
    assertArrayEquals(new Integer[]{1, 2, 3}, ArrayUtil.box(new int[]{ 1, 2, 3 }));
    assertArrayEquals(new int[]{1, 2, 3}, ArrayUtil.unbox(new Integer[]{ 1, 2, 3 }));
  }

  @Test public void testSubArray() {
    assertArrayEquals(new Integer[]{2, 3}, ArrayUtil.subArray(new Integer[]{ 1, 2, 3 }, -2));
    assertArrayEquals(new Integer[]{2}, ArrayUtil.subArray(new Integer[]{ 1, 2, 3 }, 1, 1));
  }

  @Test public void testFirstLastIndexOf() {
    TestCls[] list = buildArray();
    assertEquals(list[2], ArrayUtil.first(list, eq(F_TYPE, 2)).get());
    assertEquals(2, ArrayUtil.indexOf(list, eq(F_TYPE, 2)));

    assertEquals(Optional.empty(), ArrayUtil.first(list, eq(F_TYPE, 3)));
    assertEquals(Optional.empty(), ArrayUtil.first(null, eq(F_TYPE, 3)));
    assertEquals(-1, ArrayUtil.indexOf(null, eq(F_TYPE, 2)));
    assertTrue(ArrayUtil.contains(list, list[2]));
  }

  @Test public void testReduce() {
    String[] str = {"Hello", "world"};
    assertEquals("Hello world ", ArrayUtil.reduce(str, "", (sum, item) -> sum + item + " "));
  }
}
