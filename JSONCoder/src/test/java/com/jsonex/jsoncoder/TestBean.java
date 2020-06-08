/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.xml.datatype.XMLGregorianCalendar;
import java.beans.Transient;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("CanBeFinal")
@Accessors(chain = true)
public class TestBean extends BaseTestBean {
  @Getter @Setter private Map<String, String> treeMap = new TreeMap<>();//NOPMD
  @Getter @Setter private List<String> linkedList1 = new LinkedList<>();

  @Getter @Setter private int intField;
  @Getter @Setter private float floatField;
  @Getter @Setter private char charField;
  @Getter @Setter private boolean booleanField;
  @Getter @Setter private String strField;
  @Getter @Setter private Date dateField;
  @Getter @Setter private TestBean2 bean2;
  @Getter @Setter private Currency currency;
  @Getter @Setter private int[] ints;
  @Getter @Setter private TestBean2[] bean2s;
  @Getter final String readonlyField = "This's a readonly field";
  @Getter @Setter private AtomicInteger atomicInteger = new AtomicInteger(100);
  @Getter @Setter private BigInteger bigInteger;
  @Getter @Setter Class<?> someClass;
  @Getter @Setter Method someMethod;
  @Getter @Setter XMLGregorianCalendar xmlCalendar;
  @Getter @Setter Map<Date, Number> dateNumberMap;
  @Getter @Setter String fieldInBaseClass = "Overridden Value";

  public List<String> publicLinkedList = new LinkedList<>();
  public Map<String, String> publicTreeMap = new TreeMap<>();//NOPMD
  public String publicStrField;
  public TreeMap<String, Date> publicMap;
  public BigDecimal publicBigDecimal1;
  public BigDecimal publicBigDecimal2;
  public int[] publicInts;

  public List<TestBean2> bean2List;
  public Set<String> publicStringSet;

  public Object nullAttributes;

  @Transient public String getTransientProp() { return "TransientProp"; }
  transient public String transientField = "transientField";
  private String privateField = "privateFieldValue";
  public static String staticField = "staticFieldValue";
}
