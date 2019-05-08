/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BeanProxyTest {
  enum SampleFlags {
    FLAG1,
    FLAG2,
  }
  public interface TestBeanIntf {
    void setFlag(SampleFlags flag, boolean val);
    boolean hasFlag(SampleFlags flag);
    TestBeanIntf setChained(String chained);
    String getChained();
    long getUserId();
    void setUserId(long id);
    // Attribute without a setter method
    String getContact();
  }
  
  @Test public void testBeanProxy() {
    TestBeanIntf bean = BeanProxy.createProxy(TestBeanIntf.class);
    bean.setFlag(SampleFlags.FLAG1, true);
    assertTrue(bean.hasFlag(SampleFlags.FLAG1));
    assertFalse(bean.hasFlag(SampleFlags.FLAG2));
    
    assertEquals(0, bean.getUserId());
    bean.setChained("chained")
        .setUserId(100);
    assertEquals("chained", bean.getChained());
    assertEquals(100, bean.getUserId());
    
    final String TEST_CONTACT = "test";
    assertNull(bean.getContact());
    BeanProxy.setAttribute(bean,  "contact", TEST_CONTACT);
    assertEquals(TEST_CONTACT, bean.getContact());
  }
}
