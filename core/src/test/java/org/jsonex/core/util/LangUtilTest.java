package org.jsonex.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ExtensionMethod(LangUtil.class)
public class LangUtilTest {
  @Data @AllArgsConstructor
  static class A {
    B b;
    public void clearB() { b = null; }
  }
  @Data @RequiredArgsConstructor
  static class B {
    final C c;
  }
  @Data @RequiredArgsConstructor
  static class C {
    final String value;
  }

  @Test public void testSafe() {
    A a = new A(null);
    assertNull(a.safe(A::getB).safe(B::getC));

    a = new A(new B(new C("v")));
    assertEquals("v", a.safe(A::getB).safe(B::getC).safe(C::getValue));

    assertEquals("v", LangUtil.safe(a, A::getB, B::getC, C::getValue));
  }

  @Test public void testSafeConsume() {
    A a = new A(new B(null));
    LangUtil.doIfNotNull(a, A::clearB);
    assertNull(a.b);

    a = null;
    LangUtil.doIfNotNull(a, A::clearB);
  }

  @Test public void testDoIfInstanceOf() {
    Collection<Integer> col = Arrays.asList(1,2,3);
    LangUtil.doIfInstanceOf(col, List.class, list -> list.set(2, 0));
    assertEquals(0, ((List)col).get(2));

    Collection<Integer> col1 = ListUtil.setOf(1,2,3);
    LangUtil.doIfInstanceOf(col, List.class, list -> list.set(2, 0));
    assertEquals(ListUtil.setOf(1,2,3), col1);
  }

  @Test public void testSeq() {
    final int[] i = new int[]{0};
    Integer val = 123;
    assertEquals(val, LangUtil.seq(() -> i[0] += 1, val));
    assertEquals(val, LangUtil.seq(() -> i[0] += 1, () -> i[0] += 1, val));
    assertEquals(3, i[0]);
  }
}
