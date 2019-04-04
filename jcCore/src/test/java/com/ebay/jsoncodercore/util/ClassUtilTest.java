/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ClassUtilTest {
  private static void log(String msg) { System.out.println(msg); }//NOPMD
  @SuppressWarnings({"CanBeFinal", "SameReturnValue", "WeakerAccess"})
  public static class A {
    public String fieldA1;
    public String fieldA2;
    public String fieldA3;
    public String fieldA4;
    public String getA1(String str) { return str; }  // non-get method
    public A() {
      fieldA4 = "4";
      fieldA3 = "3";
    }
  }

  @SuppressWarnings({"CanBeFinal", "SameReturnValue", "WeakerAccess"})
  public static class B extends A {
    public String fieldB1;
    public String fieldB2;
    public String fieldB3;
    public String fieldB4;
    public String fieldB5;
    public String getFieldB1() { return fieldB1; }
  }

  @Test public void testGetProperties() {
    Map<String, BeanProperty> properties = ClassUtil.getProperties(B.class);
    log("Properties.keySet():" + properties.keySet());
    String[] exp = {"fieldA1", "fieldA2", "fieldA3", "fieldA4", "fieldB1", "fieldB2", "fieldB3", "fieldB4", "fieldB5"};
    // Java compiler will mass up the order of the getter methods, field order is preserved in most of the java versions
    assertArrayEquals(exp, properties.keySet().toArray());
  }

  @Test public void testFindCallerStack() {
    Assert.assertEquals("testFindCallerStack", ClassUtil.findCallerStackTrace(ClassUtil.class).getMethodName());
  }
}
