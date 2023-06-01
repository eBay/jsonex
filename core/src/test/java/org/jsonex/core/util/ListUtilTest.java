/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.jsonex.core.type.BeanField;
import org.jsonex.core.type.Identifiable;
import org.jsonex.core.type.Operator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import org.junit.Test;

import java.util.*;

import static org.jsonex.core.type.Operator.*;
import static org.jsonex.core.util.ListUtil.listOf;
import static org.jsonex.core.util.ListUtilTest.TestCls.*;
import static org.junit.Assert.*;

@ExtensionMethod({Operator.class, ListUtil.class})
public class ListUtilTest {
  @RequiredArgsConstructor @Getter @EqualsAndHashCode @ToString
  public static class TestCls implements Identifiable<Integer> {
    final Integer id;
    final String name;
    final int type;
    final List<String> tags;

    public final static BeanField<TestCls, String> F_NAME = new BeanField<>("name", TestCls::getName);
    public final static BeanField<TestCls, Integer> F_ID = new BeanField<>("id", TestCls::getId);
    public final static BeanField<TestCls, Integer> F_TYPE = new BeanField<>("type", TestCls::getType);
    public final static BeanField<TestCls, List<String>> F_TAGS = new BeanField<>("tags", TestCls::getTags);
  }

  private static <T> List<T> asList(T... values) {
    return new ArrayList<T>(Arrays.asList(values));
  }

  private List<TestCls> buildList() {
    return asList(
        new TestCls(0, null, 0, Arrays.asList("a", "b")),
        new TestCls(1, "name1", 1, Arrays.asList("c", "d", "e")),
        new TestCls(2, null, 2, Arrays.asList()),
        new TestCls(3, "name3", 2, null)
    );
  }

  @Test public void testMap() {
    List<String> result = ListUtil.map(buildList(), F_NAME);
    assertArrayEquals(new String[]{null, "name1", null, "name3"}, result.toArray());

    result = ListUtil.map(buildList(), TestCls::getName);
    assertArrayEquals(new String[]{null, "name1", null, "name3"}, result.toArray());

    List<Integer> lengths = ListUtil.map(buildList(), safeOf(TestCls::getName, String::length));
    assertArrayEquals(new Integer[]{null, 5, null, 5}, lengths.toArray());

    assertNull("should be null if pass null", ListUtil.map(null, F_NAME));
  }

  @Test public void testFlatMap() {
    List<String> result = ListUtil.flatMap(buildList(), F_TAGS);
    assertArrayEquals(new String[]{"a", "b", "c", "d", "e"}, result.toArray());
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

    Map<String, List<Integer>> result1 = ListUtil.mapKeys(map, String::toUpperCase);

    assertSame(map.get("key1"), result1.get("KEY1"));
    assertSame(map.get("key2"), result1.get("KEY2"));
  }

  @SuppressWarnings("unchecked")
  @Test public void testToLongArray() {
    List list = asList("1", 2, null);
    assertArrayEquals(new long[]{1, 2, 0}, ListUtil.toLongArray(list));
  }

  @Test public void testToMapWithKeyAndVal() {
    Map<Integer, String> map = ListUtil.toMap(buildList(), F_ID, TestCls::getName);
    assertArrayEquals(new String[]{null, "name1", null, "name3"}, new TreeMap(map).values().toArray());
  }

  @Test public void testToMapWithKey() {
    List<TestCls> list = buildList();
    Map<Integer, TestCls> map = ListUtil.toMap(list, F_ID);

    assertArrayEquals(list.toArray(), new TreeMap(map).values().toArray());

    TreeMap<Integer, TestCls> treeMap = ListUtil.toMapInto(list, F_ID, new TreeMap<>());
    assertArrayEquals(list.toArray(), map.values().toArray());
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

  @Test public void testIn() {
    assertTrue("should contains", ListUtil.inLongs(3, 1, 2, 3));
    assertFalse("should not contains", ListUtil.inLongs(4, 1, 2, 3));
    assertFalse("return false if source is null", ListUtil.inLongs(0, null));

    assertTrue("should contains", ListUtil.isIn("abc","abc", "def"));
    assertFalse("should not contains", ListUtil.isIn("abc","ac", "def"));
    assertFalse("return false if null not match", ListUtil.isIn(null,"abc", "def"));
    assertTrue("return true if null matches", ListUtil.isIn(null,"abc", null));
  }

  @Test public void testExits() {
    // !!!! Following two commented out statements cause Java compiler throws StackOverflowError for JDK1.8
    // Under following conditions:
    //   1. Use @ExtensionMethod and use lambada
    //   2. User static import for assertXXX or multiple lines of statement with lambada
    //Assert.assertTrue("should contains type of 1", ListUtil.exists(buildList(), obj -> obj.getType() == 1));
    //Assert.assertFalse("shouldn't contains type of 3", ListUtil.exists(buildList(), obj -> obj.getType() == 3));
    assertTrue("should contain type of 1", ListUtil.exists(buildList(), eq(F_TYPE, 1)));
    assertFalse("shouldn't contain type of 3", ListUtil.exists(buildList(), eq(F_TYPE, 3)));
    assertFalse("should return false for null source", ListUtil.exists(null, eq(F_TYPE, 3)));
  }

  @Test public void testFirstLastIndexOf() {
    List<TestCls> list = buildList();
    assertEquals(list.get(0), ListUtil.first(list).get());
    assertEquals(list.get(3), ListUtil.last(list).get());

    assertEquals(list.get(2), ListUtil.first(list, eq(F_TYPE, 2)).get());
    assertEquals(2, ListUtil.indexOf(list, eq(F_TYPE, 2)));

    assertEquals(Optional.empty(), ListUtil.first(list, eq(F_TYPE, 3)));
    assertEquals(Optional.empty(), ListUtil.first(null, eq(F_TYPE, 3)));
    assertEquals(-1, ListUtil.indexOf(null, eq(F_TYPE, 2)));
  }

  @Test public void testJoin() { assertEquals("1,2,3", ListUtil.join(new Integer[]{1,2,3}, ",")); }

  @Test public void testRemoveLast() {
    List<Integer> list = new ArrayList<>(asList(1,2,3));
    ListUtil.removeLast(list);
    assertArrayEquals(new Integer[]{1,2}, list.toArray());
  }

  @Test public void testSetOf() {
    assertTrue("set should contain all the elemtns", ListUtil.setOf(1,2,3).containsAll(asList(1,2,3))); }

  @Test public void testContainsAny() {
    assertTrue("Should return true: ", ListUtil.containsAny(ListUtil.setOf(1,2,3), 1, 2));
    assertFalse("Should return false: ", ListUtil.containsAny(ListUtil.setOf(1,2,3), 4, 5));
    assertFalse("Should return false for null source: ", ListUtil.containsAny(null, 0));

    assertTrue("Should return true: ", ListUtil.containsAny(ListUtil.setOf(1,2,3), listOf(1, 2)));
    assertFalse("Should return false: ", ListUtil.containsAny(ListUtil.setOf(1,2,3), listOf(4, 5)));
    assertFalse("Should return false for null source: ", ListUtil.containsAny(null, listOf(0)));
  }

  @Test public void testTakeBetween() {
    List list = asList(2, 4, 5, 6, 8);
    // takeWhile
    assertEquals(asList(2, 4), ListUtil.takeWhile(list, (Integer n) -> (n % 2 == 0)));
    assertEquals(asList(), ListUtil.takeWhile(list, (Integer n) -> n < 0));
    assertEquals(list, ListUtil.takeWhile(list, (Integer n) -> n > 0));
    assertNull(ListUtil.takeWhile(null, (Integer n) -> n > 0));

    // dropWhile
    assertEquals(asList(6, 8), ListUtil.dropWhile(list, (Integer n) -> n <= 5));
    assertEquals(asList(), ListUtil.dropWhile(list, (Integer n) -> n <= 8));
    assertEquals(list, ListUtil.dropWhile(list, (Integer n) -> n < 2));

    // takeBetween
    assertEquals(asList(5, 6), ListUtil.takeBetween(list, (Integer n) -> (n % 2 == 0), (Integer n) -> n < 8));
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

  @Test public void testReduce() {
    List<String> str = ListUtil.listOf("Hello", "world");
    assertEquals("Hello world ", ListUtil.reduce(str, "", (sum, item) -> sum + item + " "));
    assertEquals("Hello world ", ListUtil.reduceTo(str, new StringBuilder(), (sum, item) -> sum.append(item + " ")).toString());
  }
}
