/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.factory;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class InjectableInstanceTest {
  public interface I1 {}
  public static class C1 implements I1 {}
  public static class C2 implements I1 {}
  
  @Test public void testGet() {
    InjectableInstance<I1> i1Instance = InjectableInstance.of(C1.class);
    assertEquals(C1.class, i1Instance.get().getClass());
    
    i1Instance.setImplClass(C2.class);
    assertEquals(C2.class, i1Instance.get().getClass());
    
    i1Instance.setInstance(new C1());
    assertEquals(C1.class, i1Instance.get().getClass());
    
    i1Instance.setObjectCreator(C2::new);
    assertEquals(C2.class, i1Instance.get().getClass());
    
    i1Instance = InjectableInstance.of(C1::new);
    assertEquals(C1.class, i1Instance.get().getClass());
  }
}
