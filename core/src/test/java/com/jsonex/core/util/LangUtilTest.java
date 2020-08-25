package com.jsonex.core.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ExtensionMethod(LangUtil.class)
public class LangUtilTest {
  @Data @RequiredArgsConstructor
  static class A {
    final B b;
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
}
