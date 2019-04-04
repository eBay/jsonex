/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncoder.TestBean2.Enum1;
import com.ebay.jsoncoder.TestBean2.IdentifiableEnum;
import com.ebay.jsoncodercore.util.MapBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.xml.datatype.DatatypeFactory;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Slf4j
public class JSONCoderTest {
  @Rule final public TestName name = new TestName();
  public void log(String msg) { log.info(this.getClass().getSimpleName() + "." + name.getMethodName() + ":" + msg); }

  private static String toJSONString(Object obj, JSONCoderOption option) {
    return JSONCoder.encode(obj, option);
  }

  private static String toJSONString(Object obj){ return JSONCoder.global.encode(obj); }

  @SneakyThrows
  private TestBean buildTestBean() {
    TestBean tb = new TestBean()
        .setDateField(new Date())
        .setStrField("String1: '\"")
        .setIntField(100)
        .setInts(new int[] { 4, 3, 2, 1 })
        .setFloatField(1.4f)
        .setCharField('A')
        .setBooleanField(false)
        .setAtomicInteger(new AtomicInteger(1001))
        .setBigInteger(new BigInteger("123456789012345678901234567890"))
        .setSomeClass(java.util.Date.class)
        .setXmlCalendar(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
        .setDateNumberMap(new MapBuilder<Date, Number>().put(new Date(), 10_000).getMap());

    tb.publicInts = tb.getInts();
    tb.publicStrField = "PublicString";
    tb.publicMap = new TreeMap<>();
    tb.publicMap.put("key1", new Date(0));
    tb.publicMap.put("key2", new Date());

    tb.publicBigDecimal1 = new BigDecimal("123456789012345678901234567");
    tb.publicBigDecimal2 = new BigDecimal("12");

    tb.setBean2s(new TestBean2[] { new TestBean2("1"), new TestBean2("2")});
    tb.bean2List = Arrays.asList(new TestBean2("AAA"), new TestBean2("BBB"));
    tb.publicStringSet = new HashSet<>(Arrays.asList("str1", "str2"));
    tb.publicTreeMap.put("key1", "value1");
    tb.getTreeMap().put("key1", "value1");

    tb.publicLinkedList.add("value1");
    tb.getLinkedList1().add("value1");

    TestBean2 bean2 = new TestBean2()
        .setStrField(null)
        .setEnumField(TestBean2.Enum1.value2);
    bean2.enumField2 = IdentifiableEnum.value1;
    bean2.strEnum = TestBean2.EnumStr.value1;
    bean2.testBean = tb;   //Test cyclic
    bean2.enumMap.put(Enum1.value1, "MapValue1");

    tb.setBean2(bean2);

    return tb;
  }

  @Test public void testBasicEncoding() {
    TestBean tb = buildTestBean();
    String str = toJSONString(tb);
    log("JSONStr=" + str);

    assertTrue("Should contain identifier for Integer IdentifiableEnum", str.contains("\"enumField2\":12345"));
    assertTrue("Double quote should be escaped", str.contains("\"String1: '\\\"\""));
    assertTrue("Should contain identifier for String IdentifiableEnum", str.contains("strEnumV1"));
    assertFalse("TransientProp shouldn't be encoded", str.contains("TransientProp"));
    assertFalse("transientField shouldn't be encoded", str.contains("transientField"));
    assertFalse("privateField shouldn't be encoded", str.contains("privateField"));
    assertFalse("staticField shouldn't be encoded", str.contains("staticField"));

    TestBean tb1 = JSONCoder.global.decode(str, TestBean.class);
    assertEquals(tb1.publicStrField, tb.publicStrField);
    assertTrue(tb.publicTreeMap instanceof TreeMap);
    assertTrue(tb.getTreeMap() instanceof TreeMap);
    assertTrue(tb.publicLinkedList instanceof LinkedList);
    assertTrue(tb.getLinkedList1() instanceof LinkedList);

    assertEquals(str, toJSONString(tb1));
  }

  @Test public void testCyclicReference() {
    TestBean tb = new TestBean().setBean2(new TestBean2());
    tb.getBean2().testBean = tb;
    String str = toJSONString(tb, new JSONCoderOption().setJsonOption(false, '`', 0));
    log("JSONStr=" + str);

    assertTrue("Cyclic reference should be encoded as $ref", str.indexOf("testBean:`$ref:../../`") > 0);
    TestBean tb1 = JSONCoder.global.decode(str, TestBean.class);
    assertEquals(tb1.getBean2().testBean, tb1);
  }

  @Test public void testDedupWithRef() {
    TestBean2 tb2 = new TestBean2();
    TestBean tb = new TestBean().setBean2(tb2);
    tb.bean2List = Collections.singletonList(tb2);

    String str = toJSONString(tb, JSONCoderOption.create().setDedupWithRef(true));
    log("JSONStr=" + str);

    assertTrue("Generate ref if dedupWithRef is set", str.contains("$ref"));
    TestBean tb1 = JSONCoder.global.decode(str, TestBean.class);
    // assertNotSame(tb1.getBean2(), tb1.bean2List.get(0));
    // TODO: Fix. currently reference object can't be de-serialized due to JSONObject
    // uses HashMap which cause the Attributes out of order, so the referenced object may not be
    // de-serialized before it's referenced.
    // assertSame(tb1.getBean2(), tb1.bean2List.get(0));
  }


  @Test public void testEnumNameOption() {
    JSONCoderOption codeOption = JSONCoderOption.create().setShowEnumName(true);
    String str = toJSONString(buildTestBean(), codeOption);
    log("JSON str=" + str);
    assertTrue("Should contain both enum id and name when showEnumName is set", str.indexOf("12345-value1") > 0);
    assertEquals(str, toJSONString(JSONCoder.global.decode(str, TestBean.class), codeOption));
  }

  @Test public void testCustomQuote() {
    JSONCoderOption codeOption = JSONCoderOption.create();
    codeOption.getJsonOption().setQuoteChar('\'');
    String str = toJSONString(buildTestBean(), codeOption);
    log("strWithSingleQuote=" + str);
    assertTrue("Single quote instead of double quote should be quoted when Quote char is set to single quote",
        str.indexOf("'String1: \\'\"'") > 0);
    assertEquals(toJSONString(JSONCoder.global.decode(str, TestBean.class), codeOption), str);

    codeOption.getJsonOption().setAlwaysQuoteName(false);  // Make quote optional for attribute names
    str = toJSONString(buildTestBean(), codeOption);
    log("strWithNoKeyQuote=" + str);
    assertTrue("Key shouldn't be quoted when alwaysQuoteName is set to false", str.contains("{treeMap:"));
    assertEquals(toJSONString(JSONCoder.global.decode(str, TestBean.class), codeOption), str);

    codeOption.getJsonOption().setQuoteChar('`');
    str = toJSONString(buildTestBean(), codeOption);
    log("strWithBackQuote=" + str);
    assertTrue("back quote should be quoted when quoteChar set to back quote", str.indexOf("`String1: '\"`") > 0);
  }

  @SneakyThrows
  @Test public void testDumpOnlyOptions() {
    // Set following attributes will make it's for dump only, can't be parse back to original class
    JSONCoderOption codeOption = JSONCoderOption.create()
        .setShowEnumName(true);
    codeOption.getJsonOption().setIndentFactor(4);

    TestBean tb = buildTestBean();
    tb.getBean2().setObjs(new Object[]{"objstr1", new Date() });
    tb.setSomeMethod(Date.class.getMethod("getTime"));

    String str = toJSONString(tb, codeOption);

    log("JSONStr=" + str);
    assertTrue(str.indexOf("readonly") > 0);
    assertTrue(str.indexOf("objstr1")>0);

    codeOption.showType = true;
    codeOption.setIgnoreSubClassFields(true);
    str = toJSONString(tb, codeOption);
    log("With showType and ignoreSubClass=" + str);
    assertTrue(str.contains("$type"));

    try {
      JSONCoder.global.decode(str, TestBean.class);
    } catch(Exception e) {
      log.error("", e);
    }
  }

  @Test public void testPolymorphicType() {
    String str1 = "{intField:1, $type:'com.ebay.jsoncoder.TestBean'}";
    TestBean obj = (TestBean)JSONCoder.global.decode(str1, Object.class);

    String str2 = toJSONString(obj);
    log("str2=" + str2);
    TestBean obj1 = JSONCoder.global.decode(str2, TestBean.class);
    assertEquals(1, obj1.getIntField());

    // Invalid class
    str1 = "{intField:1, $type:'InvalidClass'}";
    try {
      JSONCoder.global.decode(str1, Object.class);
      fail("Shouldn't be success when class is not found");
    } catch(BeanCoderException e) {
      assertEquals("Incorrect $type:InvalidClass", e.getCause().getMessage());
    }
  }

  @Test public void testObjectType() {
    // Map to Object
    String str1 = "{intField:1}";
    Object obj = JSONCoder.global.decode(str1, Object.class);
    assertTrue("expect Map type, actually: " + obj.getClass() , obj instanceof Map);
    String str2 = JSONCoder.getGlobal().encode(obj);
    log("testObjectType: str2=" + str2);
    TestBean obj1 = JSONCoder.global.decode(str2, TestBean.class);

    // String to Object
    str1 = "test string";
    obj = JSONCoder.global.decode(str1, Object.class);
    assertEquals(str1,  obj);

    // Map to String
    str1 = "{strField: {key1: 'value1'}}";
    TestBean tb = JSONCoder.global.decode(str1, TestBean.class);
    log("testObjectType: tb=" + JSONCoder.global.encode(tb));
    assertEquals("{\"key1\":\"value1\"}", tb.getStrField());
  }

  private void testPrimitiveType(Object o, Class<?> cls) {
    String jsonStr = JSONCoder.global.encode(o);
    log("jsonStr=" + jsonStr);
    Object o1 = JSONCoder.global.decode(jsonStr, cls);
    log("str1=" + o);
    assertEquals(o,  o1);
  }

  @Test public void testPrimitiveTypes() {
    testPrimitiveType("test string {1} [123,234]", String.class);
    testPrimitiveType("", String.class);
    testPrimitiveType(new Date(), Date.class);

    testPrimitiveType(123.456, Double.class);

    testPrimitiveType(true, Boolean.class);
  }

  @Test public void testDecodePrimitiveTypeWithoutQuote() {
    assertEquals("1234", JSONCoder.global.decode("1234", String.class));
    assertEquals("12:34", JSONCoder.global.decode("12:34", String.class));

    String strDate = "2018-10-1 12:30:10";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
    assertEquals(strDate, df.format(JSONCoder.global.decode(strDate, Date.class)));

    strDate = "1970-01-01T00:00:00.000Z";  // ISO 8601 date format
    assertEquals(0, JSONCoder.global.decode(strDate, Date.class).getTime());
  }

  @Test public void testUnicodeAndCustomQuote() {
    JSONCoderOption opt = new JSONCoderOption();
    opt.getJsonOption().setQuoteChar('`');
    opt.getJsonOption().setIndentFactor(2);
    String jsonStr = JSONCoder.encode("\002\002", opt);
    assertEquals("`\\u0002\\u0002`",  jsonStr);
  }

  @Test public void testGenericType() {
    String str = "['str1', 'str2', 'str3']";
    List<String> result = JSONCoder.global.decode(new DecodeReq<List<String>>(str){});
    assertArrayEquals(new String[]{"str1", "str2","str3"}, result.toArray());
  }

  @Test public void testIncrementDecode() {
    String jsonStr1 = "{strField: 'strVal1', testBean: {floatField: 1.0, dateField: '2017-10-1'}}";
    String jsonStr2 = "{enumField: 'value1', testBean: {floatField: 2.0, publicStrField: 'publicStrVal'}}";

    TestBean2 bean2 = JSONCoder.global.decode(jsonStr1, TestBean2.class);
    log("bean2: " + JSONCoder.global.encode(bean2));

    bean2 = JSONCoder.global.decodeTo(jsonStr2, bean2);
    log("testIncrementDecode: merged: bean2: " + JSONCoder.global.encode(bean2));
    assertEquals("strVal1", bean2.getStrField());
    Assert.assertEquals(Enum1.value1, bean2.getEnumField());
    assertEquals(2.0, bean2.testBean.getFloatField(), 0.0001);
    assertEquals("publicStrVal", bean2.testBean.publicStrField);
  }

  @Test public void testDefaultEnums() {
    String str = "{enumField: 'value_new', enumField2: 'value_new'}";
    TestBean2 bean2 = JSONCoder.global.decode(str, TestBean2.class);
    assertEquals(TestBean2.Enum1.value1, bean2.getEnumField());
    assertNull(bean2.enumField2);
  }

  @Test public void testFilter() {
    TestBean2 bean = new TestBean2();
    bean.enumField2 = IdentifiableEnum.value1;
    bean.setStrField("str1");
    bean.testBean = new TestBean();
    bean.testBean.setStrField("str2");
    bean.testBean.publicStrField = "publicStr";
    JSONCoderOption opt = JSONCoderOption.create();
    opt.getOrAddSimpleFilter(TestBean2.class).setInclude(true).addProperties("enumField2", "testBean");
    opt.getOrAddSimpleFilter(TestBean.class).addProperties("publicStrField");
    opt.getOrAddDefaultFilter().addProperties("fieldInAnyClass");
    String result = JSONCoder.encode(bean, opt);
    log("result=" + result);
    assertTrue("shouldn't contain 'str1'", !result.contains("str1"));
    assertTrue("should contain 'enumField2'", result.contains("enumField2"));
    assertTrue("should 'str2'", result.contains("str2"));
    assertTrue("shouldn't contain 'publicStrField'", !result.contains("publicStrField"));

    opt.addSkippedClasses(TestBean.class);
    result = JSONCoder.encode(bean, opt);
    log("result=" + result);
    assertTrue("shouldn't contain class TestBean", !result.contains("testBean"));
  }

  @Test public void testInvalidClass() {
    // Invalid class
    String str = "{someClass: 'InvalidClass'}";
    try {
      JSONCoder.getGlobal().decode(str, TestBean.class);
      fail("Shouldn't be success when class is not found");
    } catch(BeanCoderException e) {
      e.printStackTrace();
      assertEquals("Can't load class: InvalidClass", e.getCause().getCause().getMessage());
    }
  }

  @Test public void testDateFormat() {
    // date as long number
    String str = "{dateField: 123456789}";
    TestBean bean = JSONCoder.getGlobal().decode(str, TestBean.class);
    assertEquals(123456789L, bean.getDateField().getTime());

    str = "{dateField: '1900-1-1'}";
    bean = JSONCoder.getGlobal().decode(str, TestBean.class);
    assertEquals("1900-01-01", new SimpleDateFormat("yyyy-MM-dd").format(bean.getDateField()));

    str = "{dateField: '1900-1-1 11:11:11'}";
    bean = JSONCoder.getGlobal().decode(str, TestBean.class);
    assertEquals("1900-01-01 11:11:11", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bean.getDateField()));

    str = "{dateField: 'invalidDateFormat'}";
    try {
      JSONCoder.getGlobal().decode(str, TestBean.class);
      fail("Shouldn't be success when date format is not correct");
    } catch (BeanCoderException e) {
      assertEquals("java.text.ParseException: Unparseable date: \"invalidDateFormat\"", e.getCause().getCause().getMessage());
    }
  }

  @Test public void testDeepClone() {
    TestBean tb = buildTestBean();
    TestBean tb2 = BeanCoder.deepClone(tb);
    assertNotSame("deep clone should have different sub-object", tb.getBean2(), tb2.getBean2());
    assertEquals(tb.getBean2().getStrField(), tb2.getBean2().getStrField());

    assertNull(BeanCoder.deepClone(null));
  }

  @Test public void testDeepCopyTo() {
    TestBean to = buildTestBean();
    TestBean from = new TestBean();
    from.setBean2(new TestBean2().setStrField("newStrValue"));
    BeanCoder.deepCopyTo(from, to);
    assertNotSame("deep clone should have different sub-object", to.getBean2(), from.getBean2());
    assertEquals(from.getBean2().getStrField(), to.getBean2().getStrField());
  }

  @Test public void testMultiLineQuotes() {
    String str = "{strField:`line1\nline2`}";
    TestBean bean = JSONCoder.getGlobal().decode(str, TestBean.class);
    assertEquals("line1\nline2", bean.getStrField());

    str = "{strField:'line1\\n\\r\\b\\t\\fline2'}";
    bean = JSONCoder.getGlobal().decode(str, TestBean.class);
    assertEquals("line1\n\r\b\t\fline2", bean.getStrField());
  }

  @Test public void testDecodeDecodeNull() {
    assertNull(JSONCoder.global.decode((String)null, Object.class));
    assertEquals("null", JSONCoder.global.encode((Object)null));
  }

  @Test public void testShowPrivateField() {
    String str = toJSONString(new TestBean(), JSONCoderOption.create().setShowPrivateField(true));
    assertTrue("privateField should be encoded when showPrivateField is set", str.contains("privateFieldValue"));
  }

  @Test public void testErrorOnUnknownField() {
    String str = "{intField: 1234, unknownField: 'test'}";
    TestBean tb = JSONCoder.global.decode(str, TestBean.class);
    assertEquals(1234, tb.getIntField());

    try {
      JSONCoder.decode(str, TestBean.class, JSONCoderOption.create().setErrorOnUnknownProperty(true));
      fail("Should fail when errorOnUnknownProperty is set and unknownField is detected");
    } catch (BeanCoderException e) {
      assertEquals("No such attribute:unknownField,class:class com.ebay.jsoncoder.TestBean", e.getCause().getMessage());
    }

    try {
      JSONCoder.decode("{transientField: 'somevalue'}", TestBean.class, JSONCoderOption.create().setErrorOnUnknownProperty(true));
      fail("Should fail when errorOnUnknownProperty is set and unknownField is detected");
    } catch (BeanCoderException e) {
      assertEquals("Field is static or transient:transientField,class:class com.ebay.jsoncoder.TestBean", e.getCause().getMessage());
    }
  }

  @Test public void testEncodeToWriter() {
    StringWriter sWriter = new StringWriter();
    JSONCoder.global.encode(new TestBean(), sWriter);
    String str = sWriter.toString();
    log("str=" + str);
    assertTrue("Encode to writer should succeed", sWriter.toString().contains("intField"));
  }
}

