package org.jsonex.core.type;

import org.junit.Assert;
import org.junit.Test;

public class UnionTest {
  @Test
  public void testUnion() {
    Union.Union2<String, Integer> str = Union.Union2.of_0("string");
    Assert.assertEquals(String.class, str.getType());
    Assert.assertEquals("string", str._0);

    Union.Union2<String, Integer> num = Union.Union2.of_1(1);
    Assert.assertEquals(Integer.class, num.getType());
    Assert.assertEquals((Integer)1, num._1);
  }
}
