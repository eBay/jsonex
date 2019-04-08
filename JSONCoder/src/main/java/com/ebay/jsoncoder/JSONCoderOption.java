/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncoder.coder.CoderDate;
import com.ebay.jsoncoder.coder.CoderXMLGregorianCalendar;
import com.ebay.jsoncoder.coder.CoderAtomicInteger;
import com.ebay.jsoncoder.coder.CoderBigInteger;
import com.ebay.jsoncoder.coder.CoderClass;
import com.ebay.jsoncoder.coder.CoderEnum;
import com.ebay.jsoncoder.treedoc.TDJSONWriter.JSONOption;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnusedReturnValue")
@Accessors(chain=true)
public class JSONCoderOption {
  @Getter final static JSONCoderOption global = new JSONCoderOption(null);
  static {
    global.coderList.add(new CoderDate());
    global.coderList.add(new CoderEnum());
    global.coderList.add(new CoderXMLGregorianCalendar());
    global.coderList.add(new CoderAtomicInteger());
    global.coderList.add(new CoderBigInteger());
    global.coderList.add(new CoderClass());

    global.skippedClasses.add(Format.class);

    global.fallbackDateFormats.add("yyyy-MM-dd HH:mm:ss.SSS.Z");  //Just for backward compatibility.
    global.fallbackDateFormats.add("yyyy/MM/dd HH:mm:ss.SSS.Z");
    global.fallbackDateFormats.add("yyyy-MM-dd HH:mm:ss.SSS");
    global.fallbackDateFormats.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
    global.fallbackDateFormats.add("yyyy-MM-dd HH:mm:ss");
    global.fallbackDateFormats.add("yyyy-MM-dd");
    global.fallbackDateFormats.add("HH:mm:ss");
    
    global.getDefaultFilter().addProperties("copy");  // DAO class has a getCopy() method
  }
  private final JSONCoderOption parent;
  
  /**
   * If true, when convert from an java bean, the readonly field will also be converted.
   */
  @Getter @Setter boolean ignoreReadOnly;
  
  /**
   * If true, subclass field won't be encoded
   */
  @Getter @Setter Boolean ignoreSubClassFields;

    
  /**
   * If true, enum name will be encoded
   */
  @Getter @Setter boolean showEnumName;
  
  /**
   * If true, class type will be encoded
   */
  @Getter @Setter boolean showType;
  
  /**
   * If true, duplicated object will be serialized as a reference to existing object's hash
   */
  @Getter @Setter boolean dedupWithRef;
  
  /**
   * If true, for java bean type, only field include private will be returned, no setter getter method will be returned.
   */
  @Getter @Setter boolean showPrivateField;
  
  /**
   * Used by BeanCoderDate, If Date format is null, date will be encoded as long with value of Date.getTime()
   */ 
  @Getter @Setter String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  @Getter private final List<String> fallbackDateFormats = new ArrayList<>();
  
  @Getter @Setter boolean alwaysMapKeyAsString;
  
  /**
   * Used by de-serializer, indicate throw exception if there's unknown field.
   */
  @Getter @Setter boolean errorOnUnknownProperty;
  
  @Getter private final Set<Class<?>> skippedClasses = new HashSet<>();
  
  //The class in this list, subclass field will be ignored.
  @Getter private final Set<Class<?>> ignoreSubClassFieldsClasses = new HashSet<>();


  @Getter private final List<IFilter> filters = new ArrayList<>();
  @Getter private final List<ICoder<?>> coderList = new ArrayList<>();
  
  /**
   * used to override equals and hashCode methods for certain object
   * e.g. BO object as there are no proper implementation of equals and hashCode 
   * which could cause duplicated copy of Object to be output.
   * 
   * The priority is based on the index of the wrapper. So if want to add highest priority
   * need to use equalsWrapper.add(0, wrapper).
   * 
   */
  @Getter private final List<EqualsWrapper<?>> equalsWrapper = new ArrayList<>();
  
  // JSON coder config
  @Getter @Setter @Delegate private JSONOption jsonOption = new JSONOption();

  public JSONCoderOption() { this(global); }
  private JSONCoderOption(JSONCoderOption parent) { this.parent = parent; }
  public static JSONCoderOption create() { return new JSONCoderOption(); }
  
  ICoder<?> findCoder(Class<?> cls){
    for (ICoder<?> bc : coderList){
      if(bc.getType().isAssignableFrom(cls))
        return bc;
    }
    return parent == null ? null : parent.findCoder(cls);
  }
  
  public boolean isClassSkipped(Class<?> cls) {
    for (Class<?> skip : skippedClasses) {
      if (skip.isAssignableFrom(cls))
        return true;
    }
    
    return parent != null && parent.isClassSkipped(cls);
  }
  
  public  boolean isFieldSkipped(Class<?> cls, String field) {
    for (IFilter filter : filters) {
      if (!filter.getType().isAssignableFrom(cls))
        continue;
      if (filter.isFieldSkipped(field) == Boolean.TRUE) 
        return true;
    }
    
    return parent != null && parent.isFieldSkipped(cls, field);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  Object getEqualsWrapper(Object obj) {
    for(EqualsWrapper ew : equalsWrapper)
      if(ew.getType().isAssignableFrom(obj.getClass()))
        return ew.newWrapper(obj);
    return parent == null ? obj : parent.getEqualsWrapper(obj);
  }
  
  public Date parseDateFullback(String dateStr) throws ParseException {
    ParseException exp = null;
    for(String fmt : fallbackDateFormats){
      try {
        return new SimpleDateFormat(fmt).parse(dateStr);
      } catch(ParseException e1) {
        exp = e1;
      }
    }
    if (parent == null)
      throw exp == null ? new ParseException(dateStr, 0) : exp;
    return parent.parseDateFullback(dateStr);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean isIgnoreSubClassFields(Class<?> cls){
    if(ignoreSubClassFields == Boolean.TRUE)
      return true;
    for(Class iCls : ignoreSubClassFieldsClasses)
      if(iCls.isAssignableFrom(cls))
        return true;
    return parent != null && parent.isIgnoreSubClassFields(cls);
  }
  
  public SimpleFilter getDefaultFilter() { return getSimpleFilterFor(Object.class); }
  public SimpleFilter getSimpleFilterFor(Class<?> cls) {
    for (IFilter filter : filters) {
      if (!(filter instanceof SimpleFilter) || filter.getType() != cls)
        continue;
      return (SimpleFilter) filter;
    }
    SimpleFilter result = SimpleFilter.of(cls);
    filters.add(0, result);
    return result;
  }
  
  public JSONCoderOption addSkippedClasses(Class<?>... cls) {
    skippedClasses.addAll(Arrays.asList(cls));
    return this;
  }

  public JSONCoderOption addIgnoreSubClassFieldsClasses(Class<?>... cls) {
    Collections.addAll(ignoreSubClassFieldsClasses, cls);
    return this;
  }

  public JSONCoderOption addCoder(ICoder<?>... codes) {
    coderList.addAll(Arrays.asList(codes));
    return this;
  }

  public JSONCoderOption setJsonOption(boolean alwaysQuoteName, char quoteChar, int indentFactor) {
    jsonOption.setAlwaysQuoteName(alwaysQuoteName)
        .setQuoteChar(quoteChar)
        .setIndentFactor(indentFactor);
    return this;
  }

}
