/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.factory;

import com.ebay.jsoncodercore.factory.InjectableFactory.CachePolicy;
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

  public interface P {}
  public static class P1 implements P {}


  @Test public void testGet() {  
    InjectableFactory<Void, I1> i1Fact = InjectableFactory.of(Void.class, C1.class);
    assertEquals(C1.class, i1Fact.get().getClass());
    assertNotSame(i1Fact.get(), i1Fact.get());

    i1Fact = InjectableFactory.of(new Function<Void, I1>() {
      @Override public I1 apply(Void param) { return new C2(); }
      
    }, CachePolicy.GLOBAL);
    assertEquals(C2.class, i1Fact.get().getClass());
    Assert.assertSame(i1Fact.get(), i1Fact.get());
    
    i1Fact.setImplClass(C1.class);
    assertEquals(C1.class, i1Fact.get().getClass());
    i1Fact.reset();

    InjectableFactory<P, I1>i2Fact = InjectableFactory.of(P.class, C3.class);
    assertEquals(C3.class, i2Fact.get(new P1()).getClass());
  }
  
  @Test public void testThreadLocalCache() throws InterruptedException {
    final InjectableFactory<Void, I1> fact = InjectableFactory.of(Void.class, C1.class, CachePolicy.THREAD_LOCAL);
    final I1 i1 = fact.get();
    Thread thread = new Thread() {//NOPMD
      public void run() {
        I1 i2 = fact.get();
        assertNotSame(i1,  i2);
        I1 i3 = fact.get();
        Assert.assertSame(i2, i3);
      }
    };
    thread.start();
    thread.join();
  }
}
