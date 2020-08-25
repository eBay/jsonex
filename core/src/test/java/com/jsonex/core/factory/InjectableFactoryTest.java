/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.factory;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;


public class InjectableFactoryTest {
  public interface I1 {}
  public static class C1 implements I1 {}
  public static class C2 implements I1 {}
  public static class C3 implements I1 {
    public C3(P1 p) {}//NOPMD
  }

  public static class C4 implements I1 {
    public C4(P1 p1, P2 p2) {}//NOPMD
  }

  public static class C5 implements I1 {
    public C5(P1 p1, P2 p2, P3 p3) {}//NOPMD
  }

  public interface P {}
  public static class P1 implements P {}
  public static class P2 {}
  public static class P3 {}

  @Test public void testGet() {
    InjectableFactory._0<I1> i1Fact = InjectableFactory._0.of(C1::new);
    assertEquals(C1.class, i1Fact.get().getClass());
    assertNotSame(i1Fact.get(), i1Fact.get());

    i1Fact = InjectableFactory._0.of(C2::new, CacheGlobal.get());
    assertEquals(C2.class, i1Fact.get().getClass());
    Assert.assertSame(i1Fact.get(), i1Fact.get());

    i1Fact.setObjectCreator((p) -> new C1());
    assertEquals(C1.class, i1Fact.get().getClass());
    i1Fact.reset();

    InjectableFactory<P1, I1> i2Fact = InjectableFactory.of(C3::new);
    assertEquals(C3.class, i2Fact.get(new P1()).getClass());
  }

  @Test public void testGet2() {
    InjectableFactory._2<P1, P2, C4> i2Fact = InjectableFactory._2.of(C4::new);
    assertEquals(C4.class, i2Fact.get(new P1(), new P2()).getClass());

    InjectableFactory._3<P1, P2, P3, C5> i3Fact = InjectableFactory._3.of(C5::new);
    assertEquals(C5.class, i3Fact.get(new P1(), new P2(), new P3()).getClass());
  }

  @Test public void testThreadLocalCache() throws InterruptedException {
    final InjectableFactory._0<I1> fact = InjectableFactory._0.of(C1::new, CacheThreadLocal.get());
    final I1 i1 = fact.get();
    Thread thread = new Thread(() -> {
      I1 i2 = fact.get();
      assertNotSame(i1, i2);
      I1 i3 = fact.get();
      Assert.assertSame(i2, i3);
    });
    thread.start();
    thread.join();
  }

  @Test public void testSetInstance() {
    InjectableFactory<Void, I1> i1Fact = InjectableFactory.of((p) -> new C2(), CacheGlobal.get());
    I1 inst = new I1() {};
    i1Fact.setInstance(inst);
    assertEquals(inst, i1Fact.get());
  }
}