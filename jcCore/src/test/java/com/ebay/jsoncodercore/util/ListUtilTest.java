/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import com.ebay.jsoncodercore.type.Field;
import com.ebay.jsoncodercore.type.Function;
import com.ebay.jsoncodercore.type.Identifiable;
import com.ebay.jsoncodercore.type.Operator;
import com.ebay.jsoncodercore.type.Predicate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.ebay.jsoncodercore.type.Operator.eq;
import static com.ebay.jsoncodercore.type.Operator.ge;
import static com.ebay.jsoncodercore.type.Operator.in;
import static com.ebay.jsoncodercore.type.Operator.not;
import static com.ebay.jsoncodercore.util.ListUtilTest.TestCls.F_ID;
import static com.ebay.jsoncodercore.util.ListUtilTest.TestCls.F_NAME;
import static com.ebay.jsoncodercore.util.ListUtilTest.TestCls.F_TYPE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@ExtensionMethod({Operator.class, ListUtil.class})
public class ListUtilTest {
  @RequiredArgsConstructor @Getter @EqualsAndHashCode @ToString
  public static class TestCls implements Identifiable<Integer> {
    final Integer id;
    final String name;
    final int type;

    public final static Function<TestCls, String> F_NAME = new Field<TestCls, String>("name") {
      @Override public String apply(TestCls param) { return param.getName(); }
    };

    public final static Function<TestCls, Integer> F_ID = new Field<TestCls, Integer>("id") {
      @Override public Integer apply(TestCls param) { return param.getId(); }
    };

    public final static Function<TestCls, Integer> F_TYPE = new Field<TestCls, Integer>("type") {
      @Override public Integer apply(TestCls param) { return param.getType(); }
    };
  }

  private List<TestCls> buildList() {
    return Arrays.asList(
        new TestCls(0, null, 0),
        new TestCls(1, "name1", 1),
        new TestCls(2, "name2", 2),
        new TestCls(3, "name3", 2));
  }

  @Test public void testMap() {
    List<String> result = ListUtil.map(buildList(), F_NAME);
    assertArrayEquals(new String[]{null, "name1", "name2", "name3"}, result.toArray());

    assertNull("should be null if pass null", ListUtil.map(null, F_NAME));
  }

  @Test public void testGetIds() {
    List<Integer> result = ListUtil.getIds(buildList());
    assertArrayEquals(new Integer[]{0, 1, 2, 3}, result.toArray());
  }

  @Test public void getGroupBy() {
    Map<Integer, List<TestCls>> result = ListUtil.groupBy(buildList(), F_TYPE);
    assertEquals(1, result.get(1).size());
    assertEquals(2, result.get(2).size());
  }

  @Test public void testMapValues() {
    Map<String, List<Integer>> map = new HashMap<>();
    map.put("key1", Arrays.asList(1, 2, 3));
    map.put("key2", Arrays.asList(1, 2));

    Map<String, Integer> result = ListUtil.mapValues(map, new Function<List<Integer>, Integer>() {
      @Override public Integer apply(List<Integer> v) { return v.size(); }
    });

    assertSame(3, result.get("key1"));
    assertSame(2, result.get("key2"));
  }

  @SuppressWarnings("unchecked")
  @Test public void toLongArray() {
    List list = Arrays.asList("1", 2, null);
    assertArrayEquals(new long[]{1, 2, 0}, ListUtil.toLongArray(list));
  }

  @Test public void testToMapWithKeyAndVal() {
    Map<Integer, String> map = ListUtil.toMap(buildList(), F_ID, F_NAME);

    assertEquals("name1", map.get(1));
    assertEquals("name2", map.get(2));
    assertEquals("name3", map.get(3));
  }

  @Test public void testToMapWithKey() {
    List<TestCls> list = buildList();
    Map<Integer, TestCls> map = ListUtil.toMap(list, F_ID);

    assertArrayEquals(list.toArray(), new TreeMap(map).values().toArray());
  }

  @Test public void testFilter() {
    List<TestCls> list = buildList();
    List<TestCls> result = ListUtil.filter(list, new Predicate<TestCls>() {
      @Override public boolean test(TestCls obj) { return obj.getType() == 2; }
    });
    assertEquals(2, result.size());

    List<TestCls> result1 = ListUtil.filter(list, eq(F_TYPE, 2));
    assertEquals(result, result1);

    result1 = ListUtil.filter(list, ge(F_TYPE, 2));
    assertEquals(result, result1);

    result = ListUtil.filter(list, not(in(F_ID, 0, 1, 2)));
    assertEquals(1, result.size());
    assertEquals(list.get(3), result.get(0));

    // Use ExtensionMethod
    result = list.filter(F_ID.le(2).and(F_TYPE.eq(2)));
    assertEquals(1, result.size());
    assertEquals(list.get(2), result.get(0));

    result = list.filter(F_NAME.eq(null));
    assertEquals(1, result.size());

    result = list.filter(F_TYPE.lt(1));
    assertEquals(1, result.size());
    assertEquals(list.get(0), result.get(0));
  }

  @Test public void testOrderBy() {
    List<TestCls> list = buildList();
    List<TestCls> result = ListUtil.orderBy(list, new Function<TestCls, Comparable>() {
      @Override public Comparable apply(TestCls param) { return param.getName(); }
    }, true);
    assertEquals(list.get(0), result.get(3));
    assertEquals(list.get(1), result.get(2));
    assertEquals(list.get(2), result.get(1));
    assertEquals(list.get(3), result.get(0));

    result = ListUtil.orderBy(list, F_NAME);
    assertEquals(list, result);
  }

  @Test public void testContains() {
    assertTrue("should contains", ListUtil.contains(new long[]{1,2,3}, 3));
    assertFalse("should not contains", ListUtil.contains(new long[]{1,2,3}, 4));
  }

  @Test public void testExits() {
    assertTrue("should contains type of 1", ListUtil.exists(buildList(), new Predicate<TestCls>() {
      @Override public boolean test(TestCls obj) { return obj.getType() == 1; }
    }));

    assertFalse("should contains type of 1", ListUtil.exists(buildList(), new Predicate<TestCls>() {
      @Override public boolean test(TestCls obj) { return obj.getType() == 3; }
    }));
  }

  @Test public void testFirstLast() {
    List<TestCls> list = buildList();
    assertEquals(list.get(0), ListUtil.first(list));
    assertEquals(list.get(3), ListUtil.last(list));

    assertEquals(list.get(2), ListUtil.first(list, eq(F_TYPE, 2)));
    assertEquals(null, ListUtil.first(list, eq(F_TYPE, 3)));
  }

  @Test public void testJoin() {
    assertEquals("1,2,3", ListUtil.join(new Integer[]{1,2,3}, ","));
  }

  @Test public void testRemoveLast() {
    List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3));
    ListUtil.removeLast(list);
    assertArrayEquals(new Integer[]{1,2}, list.toArray());
  }

  @Test public void testSetof() {
    assertTrue("set should contain all the elemtns", ListUtil.setOf(1,2,3).containsAll(Arrays.asList(1,2,3)));
  }
}
