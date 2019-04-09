/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import com.ebay.jsoncodercore.factory.Function;
import com.ebay.jsoncodercore.type.Identifiable;
import com.ebay.jsoncodercore.type.Predicator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ListUtilTest {
  @RequiredArgsConstructor @Getter @EqualsAndHashCode
  public static class TestCls implements Identifiable<Integer> {
    final Integer id;
    final String name;
    final int type;

    public final static Function<TestCls, String> F_NAME = new Function<TestCls, String>() {
      @Override public String apply(TestCls param) { return param.getName(); }
    };

    public final static Function<TestCls, Integer> F_ID = new Function<TestCls, Integer>() {
      @Override public Integer apply(TestCls param) { return param.getId(); }
    };

    public final static Function<TestCls, Integer> F_TYPE = new Function<TestCls, Integer>() {
      @Override public Integer apply(TestCls param) { return param.getType(); }
    };
  }

  private List<TestCls> buildList() {
    return Arrays.asList(
        new TestCls(1, "name1", 1),
        new TestCls(2, "name2", 2),
        new TestCls(3, "name3", 2));
  }

  @Test public void testMap() {
    List<String> result = ListUtil.map(buildList(), TestCls.F_NAME);
    assertArrayEquals(new String[]{"name1", "name2", "name3"}, result.toArray());

    assertNull("should be null if pass null", ListUtil.map(null, TestCls.F_NAME));
  }

  @Test public void testGetIds() {
    List<Integer> result = ListUtil.getIds(buildList());
    assertArrayEquals(new Integer[]{1, 2, 3}, result.toArray());
  }

  @Test public void getGroupBy() {
    Map<Integer, List<TestCls>> result = ListUtil.groupBy(buildList(), TestCls.F_TYPE);
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
    Map<Integer, String> map = ListUtil.toMap(buildList(), TestCls.F_ID, TestCls.F_NAME);

    assertEquals("name1", map.get(1));
    assertEquals("name2", map.get(2));
    assertEquals("name3", map.get(3));
  }

  @Test public void testToMapWithKey() {
    List<TestCls> list = buildList();
    Map<Integer, TestCls> map = ListUtil.toMap(list, TestCls.F_ID);

    assertEquals(list.get(0), map.get(1));
    assertEquals(list.get(1), map.get(2));
    assertEquals(list.get(2), map.get(3));
  }

  @Test public void testFilter() {
    List<TestCls> result = ListUtil.filter(buildList(), new Predicator<TestCls>() {
      @Override public boolean test(TestCls obj) { return obj.getType() == 2; }
    });
    assertEquals(2, result.size());
  }

  @Test public void testOrderBy() {
    List<TestCls> list = buildList();
    List<TestCls> result = ListUtil.orderBy(list, new Function<TestCls, Comparable>() {
      @Override public Comparable apply(TestCls param) { return param.getName(); }
    }, true);
    assertEquals(list.get(0), result.get(2));
    assertEquals(list.get(1), result.get(1));
    assertEquals(list.get(2), result.get(0));

    result = ListUtil.orderBy(list, new Function<TestCls, Comparable>() {
      @Override public Comparable apply(TestCls param) { return param.getName(); }
    });
    assertEquals(list, result);
  }

  @Test public void testContains() {
    assertTrue("should contains", ListUtil.contains(new long[]{1,2,3}, 3));
    assertFalse("should not contains", ListUtil.contains(new long[]{1,2,3}, 4));
  }

  @Test public void testExits() {
    assertTrue("should contains type of 1", ListUtil.exists(buildList(), new Predicator<TestCls>() {
      @Override public boolean test(TestCls obj) { return obj.getType() == 1; }
    }));

    assertFalse("should contains type of 1", ListUtil.exists(buildList(), new Predicator<TestCls>() {
      @Override public boolean test(TestCls obj) { return obj.getType() == 3; }
    }));
  }

  @Test public void testFirstLast() {
    List<TestCls> list = buildList();
    assertEquals(list.get(0), ListUtil.first(list));
    assertEquals(list.get(2), ListUtil.last(list));
  }

  @Test public void testJoin() {
    assertEquals("1,2,3", ListUtil.join(new Integer[]{1,2,3}, ","));
  }

  @Test public void testRemoveLast() {
    List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3));
    ListUtil.removeLast(list);
    assertArrayEquals(new Integer[]{1,2}, list.toArray());
  }
}

