/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.factory;

import lombok.RequiredArgsConstructor;
import org.junit.Test;

import static junit.framework.Assert.*;


public class InjectableFactoryTest {
  public interface I1 {}
  public static class C implements I1 {}
  @RequiredArgsConstructor
  public static class CWrapper implements I1 {
    final I1 i1;
    final String name;
  }
  public static class C0 implements I1 {}
  public static class C1 implements I1 {
    public C1(P1 p) {}
  }

  public static class C2 implements I1 {
    public C2(P1 p1, P2 p2) {}
  }

  public static class C3 implements I1 {
    public C3(P1 p1, P2 p2, P3 p3) {}
  }

  public interface P {}
  public static class P1 implements P {}
  public static class P2 {}
  public static class P3 {}

  @Test public void testGet() {
    InjectableFactory._0<I1> i1Fact = InjectableFactory._0.of(C::new);
    assertEquals(C.class, i1Fact.get().getClass());
    assertNotSame(i1Fact.get(), i1Fact.get());

    i1Fact = InjectableFactory._0.of(C0::new, ScopeGlobal.get());
    assertEquals(C0.class, i1Fact.get().getClass());
    assertSame(i1Fact.get(), i1Fact.get());

    i1Fact.setObjectCreator((p) -> new C());
    assertEquals(C.class, i1Fact.get().getClass());
    i1Fact.reset();

    InjectableFactory<P1, I1> i2Fact = InjectableFactory.of(C1::new);
    assertEquals(C1.class, i2Fact.get(new P1()).getClass());
  }

  @Test public void testGet2() {
    InjectableFactory._2<P1, P2, C2> i2Fact = InjectableFactory._2.of(C2::new);
    assertEquals(C2.class, i2Fact.get(new P1(), new P2()).getClass());

    InjectableFactory._3<P1, P2, P3, C3> i3Fact = InjectableFactory._3.of(C3::new);
    assertEquals(C3.class, i3Fact.get(new P1(), new P2(), new P3()).getClass());
  }

  @Test public void testThreadLocalCache() throws InterruptedException {
    final InjectableFactory._0<I1> fact = InjectableFactory._0.of(C::new, ScopeThreadLocal.get());
    final I1[] objs = new I1[3];
    objs[0] = fact.get();
    Thread thread = new Thread(() -> {
      objs[1] = fact.get();
      objs[2] = fact.get();
    });
    thread.start();
    thread.join();
    assertNotSame(objs[0], objs[1]);
    assertSame(objs[2], objs[2]);
  }

  @Test public void testSetInstance() {
    InjectableFactory<Void, I1> i1Fact = InjectableFactory.of((p) -> new C0(), ScopeGlobal.get());
    I1 inst = new I1() {};
    i1Fact.setInstance(inst);
    assertEquals(inst, i1Fact.get());
  }

  @Test public void testHandlers() {
    InjectableFactory._0<I1> i1Fact = InjectableFactory._0.of(C::new);
    i1Fact.getGlobalCreateHandlers().add((i1) -> new CWrapper((I1)i1, "global"));
    i1Fact.getCreateHandlers().add((i1) -> new CWrapper(i1, "local"));
    I1 obj = i1Fact.get();
    i1Fact.getGlobalCreateHandlers().clear(); // Clear global handler to avoid interfere other tests
    assertTrue(obj instanceof CWrapper);
    CWrapper localWrapp = (CWrapper)obj;
    assertEquals("local", localWrapp.name);
    CWrapper globalWrapper = (CWrapper)localWrapp.i1;
    assertEquals("global", globalWrapper.name);
  }
}