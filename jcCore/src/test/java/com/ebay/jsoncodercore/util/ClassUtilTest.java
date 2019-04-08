/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ebay.jsoncodercore.type.TypeRef;
import com.ebay.jsoncodercore.util.ListUtilTest.TestCls;
import com.ebay.jsoncodercore.util.TestClass.TestSubClass;
import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ClassUtilTest {
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
    @NonNull
    @Getter @Setter private boolean fieldB6;
    public String getFieldB1() { return fieldB1; }
  }

  @Test public void testGetProperties() {
    Map<String, BeanProperty> properties = ClassUtil.getProperties(B.class);
    log.info("Properties.keySet():" + properties.keySet());
    String[] exp = {"fieldA1", "fieldA2", "fieldA3", "fieldA4", "fieldB1", "fieldB2", "fieldB3", "fieldB4", "fieldB5", "fieldB6"};
    // Java compiler will mass up the order of the getter methods, field order is preserved in most of the java versions
    assertArrayEquals(exp, properties.keySet().toArray());

    BeanProperty prop = properties.get("fieldB6");
    assertEquals("fieldB6", prop.getName());
    B b = new B();
    prop.set(b, true);
    assertEquals(true, prop.get(b));
    assertEquals(false, prop.isImmutable(false));
    assertEquals(true, prop.isReadable(false));
    assertEquals(Boolean.TYPE, prop.getType());
  }

  @Test public void testFindCallerStack() {
    assertEquals("testFindCallerStack", ClassUtil.findCallerStackTrace(ClassUtil.class).getMethodName());
  }

  @Test public void testSetPrivateField() {
    ClassUtil.setStaticPrivateField("com.ebay.jsoncodercore.util.TestClass", "privateStateField", "newValue");
    assertEquals("newValue", TestClass.getPrivateStateField());

    TestClass testClass = new TestClass();
    ClassUtil.setPrivateField(testClass, "privateFinalField", "newValue");
    assertEquals("newValue", testClass.getPrivateFinalField());
  }

  @SneakyThrows
  @Test public void testGetDeclaredField() {
    Field field = ClassUtil.getDeclaredField(TestSubClass.class, "privateFinalField");
    assertEquals("privateFinalField", field.getName());
  }

  @SneakyThrows
  @Test public void testGetObjectByPath() {
    String path = "java.lang.System/properties.0";
    Object val = ClassUtil.getObjectByPath(path);
    log.info("testGetObjectByPath: path=" + path + "; val=" + val );
    assertNotNull("get path should return value", val);
  }

  @Test public void testGetGenericType() {
    assertEquals(List.class, ClassUtil.getGenericClass(List.class));
    assertEquals(List.class, ClassUtil.getGenericClass(new TypeRef<List<Integer>>() {}.getType()));
  }

  @Test public void testStringToSimpleObject() {
    BeanConvertContext ctx = new BeanConvertContext();
    assertEquals((Byte)(byte)1, ClassUtil.stringToSimpleObject("1", Byte.class, ctx));
    assertEquals((Integer)1, ClassUtil.stringToSimpleObject("1", Integer.class, ctx));
    assertEquals((Long)1L, ClassUtil.stringToSimpleObject("1", Long.class, ctx));
    assertEquals((Double) 1.1d, ClassUtil.stringToSimpleObject("1.1", Double.class, ctx));
    assertEquals(Boolean.TRUE, ClassUtil.stringToSimpleObject("true", Boolean.class, ctx));
    assertEquals((Character)'a', ClassUtil.stringToSimpleObject("a", Character.class, ctx));
    assertEquals(TimeUnit.SECONDS, ClassUtil.stringToSimpleObject("SECONDS", TimeUnit.class, ctx));
  }

  @Test public void testIsSimpleType() {
    assertTrue("Integer is a simple type", ClassUtil.isSimpleType(Integer.class));
    assertFalse("Date is not a simple type", ClassUtil.isSimpleType(Date.class));
  }

  @Test public void testObjectToSimpleType() {
    assertEquals((Integer)100, ClassUtil.objectToSimpleType(Long.valueOf(100), Integer.class));
    assertEquals((Float)(float)100.1, ClassUtil.objectToSimpleType(Double.valueOf(100.1), float.class));
  }

}
