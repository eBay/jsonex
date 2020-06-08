/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.jsonex.core.type.Field;
import com.jsonex.core.type.Identifiable;
import com.jsonex.core.type.Operator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;

import static com.jsonex.core.type.Operator.*;
import static com.jsonex.core.util.ListUtil.containsAny;
import static com.jsonex.core.util.ListUtil.setOf;
import static com.jsonex.core.util.ListUtilTest.TestCls.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@ExtensionMethod({Operator.class, ListUtil.class})
public class ListUtilTest {
  @RequiredArgsConstructor @Getter @EqualsAndHashCode @ToString
  public static class TestCls implements Identifiable<Integer> {
    final Integer id;
    final String name;
    final int type;

    public final static Field<TestCls, String> F_NAME = new Field<>("name", TestCls::getName);
    public final static Field<TestCls, Integer> F_ID = new Field<>("id", TestCls::getId);
    public final static Field<TestCls, Integer> F_TYPE = new Field<>("type", TestCls::getType);
  }

  private static <T> List<T> asList(T... values) {
    return new ArrayList<T>(Arrays.asList(values));
  }

  private List<TestCls> buildList() {
    return asList(
        new TestCls(0, null, 0),
        new TestCls(1, "name1", 1),
        new TestCls(2, null, 2),
        new TestCls(3, "name3", 2)
    );
  }

  @Test public void testMap() {
    List<String> result = ListUtil.map(buildList(), F_NAME);
    assertArrayEquals(new String[]{null, "name1", null, "name3"}, result.toArray());

    result = ListUtil.map(buildList(), TestCls::getName);
    assertArrayEquals(new String[]{null, "name1", null, "name3"}, result.toArray());

    assertNull("should be null if pass null", ListUtil.map(null, F_NAME));
  }

  @Test public void testGetIds() {
    List<Integer> result = ListUtil.getIds(buildList());
    assertArrayEquals(new Integer[]{0, 1, 2, 3}, result.toArray());
  }

  @Test public void testGroupBy() {
    Map<Integer, List<TestCls>> result = ListUtil.groupBy(buildList(), F_TYPE);
    assertEquals(1, result.get(1).size());
    assertEquals(2, result.get(2).size());
  }

  @Test public void testMapValues() {
    Map<String, List<Integer>> map = new HashMap<>();
    map.put("key1", asList(1, 2, 3));
    map.put("key2", asList(1, 2));

    Map<String, Integer> result = ListUtil.mapValues(map, (v) -> v.size());

    assertSame(3, result.get("key1"));
    assertSame(2, result.get("key2"));
  }

  @SuppressWarnings("unchecked")
  @Test public void testToLongArray() {
    List list = asList("1", 2, null);
    assertArrayEquals(new long[]{1, 2, 0}, ListUtil.toLongArray(list));
  }

  @Test public void testToMapWithKeyAndVal() {
    Map<Integer, String> map = ListUtil.toMap(buildList(), F_ID, F_NAME);
    assertArrayEquals(new String[]{null, "name1", null, "name3"}, new TreeMap(map).values().toArray());
  }

  @Test public void testToMapWithKey() {
    List<TestCls> list = buildList();
    Map<Integer, TestCls> map = ListUtil.toMap(list, F_ID);

    assertArrayEquals(list.toArray(), new TreeMap(map).values().toArray());
  }

  @Test public void testFilter() {
    List<TestCls> list = buildList();

    // !!! For Jdk11,12 Following statement with @ExtensionMethod will cause exception of
    // java.lang.ClassCastException: class com.sun.tools.javac.code.Symbol$ClassSymbol cannot be cast to class com.sun.tools.javac.code.Symbol$MethodSymbol (com.sun.tools.javac.code.Symbol$ClassSymbol and com.sun.tools.javac.code.Symbol$MethodSymbol are in module jdk.compiler of loader 'app')
    //List<TestCls> result = ListUtil.filter(list, it -> it.getType() == 2);
    List<TestCls> result = ListUtil.filter(list, eq(TestCls::getType, 2));
    assertEquals(2, result.size());

    List<TestCls> result1 = ListUtil.filter(list, ge(F_TYPE, 2));
    assertEquals(result, result1);

    result = ListUtil.filter(list, not(in(F_ID, 0, 1, 2)));
    assertEquals(1, result.size());
    assertEquals(list.get(3), result.get(0));

    // Use ExtensionMethod
    result = list.filter(F_ID.le(2).and(F_TYPE.eq(2)));
    assertEquals(1, result.size());
    assertEquals(list.get(2), result.get(0));

    result = list.filter(F_NAME.eq(null));
    assertEquals(2, result.size());

    result = list.filter(F_TYPE.lt(1));
    assertEquals(1, result.size());
    assertEquals(list.get(0), result.get(0));

    result = list.filter(F_TYPE.le(1));
    assertEquals(2, result.size());
  }

  @Test public void testOrderBy() {
    List<TestCls> list = buildList();
    List<TestCls> result = ListUtil.orderBy(list, TestCls::getName, true);
    assertArrayEquals(new TestCls[] {list.get(3), list.get(1), list.get(0), list.get(2)}, result.toArray());

    result = ListUtil.orderBy(list, F_NAME);
    assertArrayEquals(new TestCls[] {list.get(0), list.get(2), list.get(1), list.get(3)}, result.toArray());
  }

  @Test public void testContains() {
    assertTrue("should contains", ListUtil.contains(new long[]{1,2,3}, 3));
    assertFalse("should not contains", ListUtil.contains(new long[]{1,2,3}, 4));
  }

  @Test public void testExits() {
    // !!!! Following two commented out statements cause Java compiler throws StackOverflowError for JDK1.8
    // Under following conditions:
    //   1. Use @ExtensionMethod and use lambada
    //   2. User static import for assertXXX or multiple lines of statement with lambada
    //Assert.assertTrue("should contains type of 1", ListUtil.exists(buildList(), obj -> obj.getType() == 1));
    //Assert.assertFalse("shouldn't contains type of 3", ListUtil.exists(buildList(), obj -> obj.getType() == 3));
    assertTrue("should contains type of 1", ListUtil.exists(buildList(), eq(F_TYPE, 1)));
    assertFalse("shouldn't contains type of 3", ListUtil.exists(buildList(), eq(F_TYPE, 3)));
  }

  @Test public void testFirstLast() {
    List<TestCls> list = buildList();
    assertEquals(list.get(0), ListUtil.first(list));
    assertEquals(list.get(3), ListUtil.last(list));

    assertEquals(list.get(2), ListUtil.first(list, eq(F_TYPE, 2)));
    assertEquals(null, ListUtil.first(list, eq(F_TYPE, 3)));
  }

  @Test public void testJoin() { assertEquals("1,2,3", ListUtil.join(new Integer[]{1,2,3}, ",")); }

  @Test public void testRemoveLast() {
    List<Integer> list = new ArrayList<>(asList(1,2,3));
    ListUtil.removeLast(list);
    assertArrayEquals(new Integer[]{1,2}, list.toArray());
  }

  @Test public void testSetOf() {
    assertTrue("set should contain all the elemtns", setOf(1,2,3).containsAll(asList(1,2,3))); }

  @Test public void testContainsAny() {
    assertTrue("containsAny should return true: ", containsAny(setOf(1,2,3), 1, 2));
    assertFalse("containsAny should return false: ", containsAny(setOf(1,2,3), 4, 5));
  }

  @Test public void testTakeWhile() {
    List list = asList(2, 4, 5, 6, 8);
    assertEquals(asList(2, 4), ListUtil.takeWhile(list, (Integer n) -> (n % 2 == 0)));
    assertEquals(asList(), ListUtil.takeWhile(list, (Integer n) -> (n < 0)));
    assertEquals(list, ListUtil.takeWhile(list, (Integer n) -> (n > 0)));
  }

  @Test public void testMergeWith() {
    Map<String, List<Integer>> target = new MapBuilder<String, List<Integer>>()
        .put("key1", asList(1, 2, 3))
        .put("key2", asList(1, 2)).getMap();

    Map<String, List<Integer>> source = new MapBuilder<String, List<Integer>>()
        .put("key2", asList(4,5))
        .put("key3", asList(7,8)).getMap();

    Map<String, List<Integer>> result = new MapBuilder<String, List<Integer>>()
        .put("key1", asList(1, 2, 3))
        .put("key2", asList(1, 2, 4, 5))
        .put("key3", asList(7, 8)).getMap();

    assertEquals(result, ListUtil.mergeWith(target, source));
  }
}
