/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.jsonex.core.type.TypeRef;
import com.jsonex.core.util.TestClass.TestSubClass;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.beans.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
    @Getter(onMethod = @__({@Transient})) @Setter private boolean fieldB6;
    public String getFieldB1() { return fieldB1; }
  }

  @SuppressWarnings({"CanBeFinal", "SameReturnValue", "WeakerAccess"})
  class C {
    public String getMethodReadOnly() { return "readonly"; }
    public void setMethodSetOnly(String str) {
      if (str == null)  // Simulate exception in setter
        throw new NullPointerException();
    }
  }

  @SneakyThrows
  @Test public void testGetProperties() {
    Map<String, BeanProperty> properties = ClassUtil.getProperties(B.class);
    log.info("Properties.keySet():" + properties.keySet());
    String[] exp = {"fieldA1", "fieldA2", "fieldA3", "fieldA4", "fieldB1", "fieldB2", "fieldB3", "fieldB4", "fieldB5", "fieldB6"};
    // Java compiler will mass up the order of the getter methods, field order is preserved in most of the java versions
    assertArrayEquals(exp, properties.keySet().toArray());

    // Field with setter/getters
    BeanProperty prop = properties.get("fieldB6");
    assertEquals("fieldB6", prop.getName());
    B b = new B();
    prop.set(b, true);
    assertEquals(true, prop.get(b));
    assertFalse(prop.isImmutable(false));
    assertTrue(prop.isReadable(false));
    assertEquals(Boolean.TYPE, prop.getType());
    assertEquals(Boolean.TYPE, prop.getGenericType());
    assertEquals(B.class.getMethod("setFieldB6", new Class[]{Boolean.TYPE}), prop.getSetter());
    assertEquals(B.class.getMethod("isFieldB6", new Class[0]), prop.getGetter());
    assertTrue(prop.isTransient());
    assertFalse(prop.isFieldAccessible(false));
    assertNotNull("Transient is annotated", prop.getAnnotation(Transient.class));

    // Field without setter/getters
    prop = properties.get("fieldB2");
    prop.set(b, "str");
    assertNull(prop.getSetter());
    assertNull(prop.getGetter());
    assertNotNull(prop.getField());
    assertEquals("str", prop.get(b));
  }

  @Test public void testGetPropertiesWithExceptions () {
    Map<String, BeanProperty> properties = ClassUtil.getProperties(C.class);
    C c = new C();

    // Reader only method
    BeanProperty prop = properties.get("methodReadOnly");
    assertNull(prop.getSetter());
    assertTrue(prop.isImmutable(true));
    assertEquals("readonly", prop.get(c));
    try {
      prop.set(c, "str");
    } catch(InvokeRuntimeException e) {
      assertEquals("field is not mutable: methodReadOnly,class:class com.jsonex.core.util.ClassUtilTest$C",
          e.getMessage());
    }

    // Set only method
    prop = properties.get("methodSetOnly");
    assertNull(prop.getGetter());
    assertFalse(prop.isReadable(true));
    prop.set(c, "str");
    try {
      prop.get(c);
    } catch(InvokeRuntimeException e) {
      assertEquals("field is not readable: methodSetOnly,class:class com.jsonex.core.util.ClassUtilTest$C",
          e.getMessage());
    }

    try {
      prop.set(c, null);
    } catch(InvokeRuntimeException e) {
      assertEquals("error set value:methodSetOnly,class=class com.jsonex.core.util.ClassUtilTest$C,value=null",
          e.getMessage());
    }
  }

  @Test public void testFindCallerStack() {
    assertEquals("testFindCallerStack", ClassUtil.findCallerStackTrace(ClassUtil.class).getMethodName());
  }

  @SuppressWarnings("deprecation")
  @Test public void testSetPrivateField() {
    ClassUtil.setStaticPrivateField("com.jsonex.core.util.TestClass", "privateStateField", "newValue");
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

  @Test public void testGetGenericClass() {
    assertEquals(List.class, ClassUtil.getGenericClass(List.class));
    assertEquals(List.class, ClassUtil.getGenericClass(new TypeRef<List<Integer>>() {}.getType()));
  }

  @SneakyThrows
  @Test public void testGetGenericTypeActualParams() {
    assertArrayEquals(new Type[]{ String.class, Integer.class },
        ClassUtil.getGenericTypeActualParams(TestClass.class.getDeclaredField("stringIntMap").getGenericType()));
  }

  @Test public void testGetGenericTypeActualParamsForInterface() {
    assertArrayEquals(new Type[]{TestClass.class},
        ClassUtil.getGenericTypeActualParamsForInterface(TestClass.class, Comparable.class));
  }

  @Test public void testGetAllInterface() {
    Set<Class<?>> intfs = new HashSet<>(ClassUtil.getAllInterface(TestClass.class));
    log.info("intfs:" + intfs);
    assertTrue(intfs.containsAll(Arrays.asList(Comparable.class, List.class, Collection.class, Cloneable.class, Serializable.class)));
  }

  @Test public void testFindInterface() {
    assertEquals(List.class, ClassUtil.findInterface(TestClass.class, Collection.class));
  }

  @SneakyThrows
  @Test public void testGetType() {
    assertEquals(Integer.class, ClassUtil.getType("java.lang.Integer"));
    assertEquals(Integer.TYPE, ClassUtil.getType("int"));
    assertEquals(Double.TYPE, ClassUtil.getType("java.lang.Double/TYPE"));
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
    assertEquals((Integer)100, ClassUtil.objectToSimpleType(100, Integer.class));
    assertEquals((Float)(float)100.1, ClassUtil.objectToSimpleType(100.1, float.class));
  }

}
