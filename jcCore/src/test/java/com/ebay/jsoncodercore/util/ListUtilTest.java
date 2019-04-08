/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import com.ebay.jsoncodercore.factory.Function;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ListUtilTest {
  @RequiredArgsConstructor
  public static class TestCls {
    final int id;
    final String name;
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

  @Test public void testToMap() {
    Collection<TestCls> testClses = Arrays.asList(
        new TestCls(1, "name1"),
        new TestCls(2, "name2"));

    Map<Integer, String> map = ListUtil.toMap(testClses,
        new Function<TestCls, Integer>() {@Override public Integer apply(TestCls p) { return p.id; }},
        new Function<TestCls, String>() {@Override public String apply(TestCls p) { return p.name; }}
    );

    assertEquals("name1", map.get(1));
    assertEquals("name2", map.get(2));
  }

  @Test public void testRemoveLast() {
    List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3));
    ListUtil.removeLast(list);
    assertArrayEquals(new Integer[]{1,2}, list.toArray());
  }
}

