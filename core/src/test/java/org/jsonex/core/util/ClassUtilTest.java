/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.jsonex.core.annotation.DefaultEnum;
import org.jsonex.core.type.Nullable;
import org.jsonex.core.type.TypeRef;
import org.jsonex.core.util.TestClass.TestSubClass;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.beans.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
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
    public String getA1(String str) { return str; }  // non-getter method with additional parameter
    public A() {
      fieldA4 = "4";
      fieldA3 = "3";
    }
  }

  @SuppressWarnings({"CanBeFinal", "SameReturnValue", "WeakerAccess"})
  public static class B extends A {
    public String fieldB1;
    @Nullable
    public transient String fieldB2;
    public String fieldB3;
    public String fieldB4;
    public String fieldB5;
    @Getter(onMethod = @__({@Transient})) @Setter private boolean fieldB6;
    public String getFieldB1() { return fieldB1; }
    @Nullable @Transient
    public void setWriteOnly(String str) { fieldB5 = str; }
    public String getReadOnly() { return fieldB1; }

    public boolean hasFieldWithHasCheck() { return false; };
    public String getFieldWithHasCheck() { throw new IllegalStateException("fieldWithHasCheck is not available"); }

    public boolean isFieldWithUnionCheck() { return false; };
    public String getFieldWithUnionCheck() { throw new IllegalStateException("fieldWithUnionCheck is not available"); }

    // To support Java 17 Record feature
    private String fieldWithSameGetterMethodName;
    public String fieldWithSameGetterMethodName() { return fieldWithSameGetterMethodName; }

  }

  @SuppressWarnings({"CanBeFinal", "SameReturnValue", "WeakerAccess"})
  class C {
    public String getMethodReadOnly() { return "readonly"; }
    public void setMethodSetOnly(String str) {
      if (str == null)  // Simulate exception in setter
        throw new NullPointerException();
    }
    public String get() { return "emptyName"; }
  }

  @SneakyThrows
  @Test public void testGetProperties() {
    Map<String, BeanProperty> properties = ClassUtil.getProperties(B.class);
    log.info("Properties.keySet():" + properties.keySet());
    String[] exp = {"fieldA1", "fieldA2", "fieldA3", "fieldA4", "fieldB1", "fieldB2", "fieldB3", "fieldB4", "fieldB5", "fieldB6",
        "fieldWithSameGetterMethodName", "fieldWithHasCheck", "fieldWithUnionCheck", "readOnly", "writeOnly"};
    // Java compiler will mess up the order of the getter methods, field order is preserved in most of the java versions
    // assertArrayEquals(exp, properties.keySet().toArray());  // This will fail
    assertEquals(ListUtil.setOf(exp), properties.keySet());

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
    assertEquals(Modifier.PUBLIC, prop.getModifier());
    assertNotNull("Transient is annotated", prop.getAnnotation(Transient.class));

    // Field without setter/getters
    prop = properties.get("fieldB2");
    prop.set(b, "str");
    assertNull(prop.getSetter());
    assertNull(prop.getGetter());
    assertNotNull(prop.getField());
    assertEquals("str", prop.get(b));
    assertEquals(String.class, prop.getType());
    assertEquals(String.class, prop.getGenericType());
    assertNotNull(prop.getAnnotation(Nullable.class));
    assertNull(prop.getAnnotation(DefaultEnum.class));
    assertEquals(Modifier.PUBLIC | Modifier.TRANSIENT, prop.getModifier());
    assertTrue(prop.isTransient());

    // Set only field
    prop = properties.get("writeOnly");
    prop.set(b, "str");
    assertEquals("str", b.fieldB5);
    assertEquals(String.class, prop.getType());
    assertEquals(String.class, prop.getGenericType());
    assertNotNull(prop.getAnnotation(Nullable.class));
    assertNull(prop.getAnnotation(DefaultEnum.class));
    assertEquals(Modifier.PUBLIC, prop.getModifier());
    assertTrue(prop.isTransient());

    // Has checker
    assertNull(properties.get("fieldWithHasCheck").get(b));
    assertNull(properties.get("fieldWithUnionCheck").get(b));

    // Support getter with the same name as field name to support java 17 Record pattern
    prop = properties.get("fieldWithSameGetterMethodName");
    assertEquals("fieldWithSameGetterMethodName", prop.getter.getName());
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
      assertEquals("field is not mutable: methodReadOnly, class:class org.jsonex.core.util.ClassUtilTest$C",
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
      assertEquals("field is not readable: methodSetOnly, class:class org.jsonex.core.util.ClassUtilTest$C",
          e.getMessage());
    }

    try {
      prop.set(c, null);
    } catch(InvokeRuntimeException e) {
      assertEquals("error set value:methodSetOnly, class=class org.jsonex.core.util.ClassUtilTest$C,value=null",
          e.getMessage());
    }
  }

  @Test public void testFindCallerStack() {
    assertEquals("testFindCallerStack", ClassUtil.findCallerStackTrace(ClassUtil.class).getMethodName());
  }

  @SuppressWarnings("deprecation")
  @Test public void testSetPrivateField() {
    ClassUtil.setStaticPrivateField("org.jsonex.core.util.TestClass", "privateStateField", "newValue");
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

  @Test public void testGetPropertyValue() {
    List<Integer> ints = ListUtil.listOf(0, 1, 2, 3, 4);
    assertEquals(3, ClassUtil.getPropertyValue(ints, "3"));

    assertEquals(new Date().getTime(),
        ((Long)ClassUtil.getPropertyValue(Date.class, "time")), 1000.);

    assertEquals(System.err,
        ClassUtil.getPropertyValue(System.class, "err"));
  }

  static class GenericTypeTestCls<T extends Integer> {
    public T boundedInt;  // Test for WildcardType
    public Collection<? extends Integer> wildCollection;  // Test for WildcardType
  }

  @SneakyThrows
  @Test public void testGetGenericClass() {
    assertNull(ClassUtil.getGenericClass(null));
    assertEquals(List.class, ClassUtil.getGenericClass(List.class));
    assertEquals(List.class, ClassUtil.getGenericClass(new TypeRef<List<Integer>>() {}.getType()));
    assertEquals((new int[0]).getClass(), ClassUtil.getGenericClass(new TypeRef<int[]>() {}.getType()));
    assertEquals(Integer.class, ClassUtil.getGenericClass(GenericTypeTestCls.class.getField("boundedInt").getGenericType()));

    Type collectionType = GenericTypeTestCls.class.getField("wildCollection").getGenericType();
    Type paramType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
    assertEquals(Integer.class, ClassUtil.getGenericClass(paramType));

    assertEquals(new Class<?>[0].getClass(), ClassUtil.getGenericClass(new TypeRef<Class<?>[]>() {}.getType()));
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

  @Test public void testToSimpleObjectWithType() {
    BeanConvertContext ctx = new BeanConvertContext();
    assertEquals((Byte)(byte)1, ClassUtil.toSimpleObject("1", Byte.class, ctx));
    assertEquals((Integer)1, ClassUtil.toSimpleObject("1", Integer.class, ctx));
    assertEquals((Long)1L, ClassUtil.toSimpleObject("1", Long.class, ctx));
    assertEquals((Double) 1.1d, ClassUtil.toSimpleObject("1.1", Double.class, ctx));
    assertEquals(Boolean.TRUE, ClassUtil.toSimpleObject("true", Boolean.class, ctx));
    assertEquals((Character)'a', ClassUtil.toSimpleObject("a", Character.class, ctx));
    assertEquals(TimeUnit.SECONDS, ClassUtil.toSimpleObject("SECONDS", TimeUnit.class, ctx));
  }

  @Test public void testToSimpleObjectWithoutType() {
    assertEquals(1, ClassUtil.toSimpleObject("1"));
    assertEquals(1000000000000L, ClassUtil.toSimpleObject("1000000000000"));
    assertEquals(1.0, ClassUtil.toSimpleObject("1.0"));
    assertEquals(Boolean.TRUE, ClassUtil.toSimpleObject("true"));
    assertEquals(Boolean.FALSE, ClassUtil.toSimpleObject("false"));
    assertEquals(0x11, ClassUtil.toSimpleObject("0x11"));
    assertEquals(0X11, ClassUtil.toSimpleObject("0X11"));
    assertEquals("1.1.1.1", ClassUtil.toSimpleObject("1.1.1.1"));
  }

  @Test public void testIsSimpleType() {
    assertTrue("Integer is a simple type", ClassUtil.isSimpleType(Integer.class));
    assertFalse("Date is not a simple type", ClassUtil.isSimpleType(Date.class));
  }

  @Test public void testObjectToSimpleType() {
    assertEquals(100, ClassUtil.objectToSimpleType(100, Integer.class));
    assertEquals(100.1f, ClassUtil.objectToSimpleType(100.1, float.class));
  }

  @SneakyThrows
  @Test public void testFindMethod() {
    MethodWrapper mw = ClassUtil.findMethod(HashMap.class, "<init>", 2, null);
    assertEquals(HashMap.class.getConstructor(int.class, float.class), mw.getConstructor());
    assertEquals(new HashMap<>(), mw.invoke(null, new Object[]{1, 0.7f}));
    assertEquals("java.util.HashMap/<init>(int arg0, float arg1)", mw.toString());

    mw = ClassUtil.findMethod(HashMap.class, "<init>", 1, new Class[]{TreeMap.class});
    assertEquals(HashMap.class.getConstructor(Map.class), mw.getConstructor());
  }
}
