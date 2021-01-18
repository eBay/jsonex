/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jsonex.core.factory.CacheThreadLocal;
import org.jsonex.core.factory.InjectableFactory;
import org.jsonex.core.type.Tuple;
import org.jsonex.core.type.Tuple.Pair;
import org.jsonex.core.util.BeanProperty;
import org.jsonex.jsoncoder.coder.*;
import org.jsonex.jsoncoder.fieldTransformer.FieldTransformer;
import org.jsonex.jsoncoder.fieldTransformer.SimpleFilter;
import org.jsonex.treedoc.json.TDJSONOption;
import org.slf4j.Logger;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jsonex.core.util.LangUtil.*;

@SuppressWarnings("UnusedReturnValue")
@Accessors(chain=true)
public class JSONCoderOption {
  @Getter final static JSONCoderOption global = new JSONCoderOption(null);
  static {
    global.addCoder(CoderDate.get(), CoderEnum.get(), CoderXMLGregorianCalendar.get(), CoderAtomicInteger.get(),
        CoderBigInteger.get(), CoderClass.get(), CoderURI.get(), CoderURL.get());

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
   * If true, when convert from an java bean, the readonly field will be ignored
   */
  @Getter @Setter boolean ignoreReadOnly;
  
  /**
   * If true, subclass field won't be encoded
   */
  @Getter @Setter boolean ignoreSubClassFields;
    
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
  
  @Getter @Setter String parsingDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  /**
   * Used by {@link CoderDate}.encode()}, If Date format is null, date will be encoded as long with value of Date.getTime()
   */
  @Getter @Setter String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  /** Used by {@link CoderDate}.encode, by default it will use default timezone, a custom timezone can be specified*/
  @Getter @Setter TimeZone timeZone = null;
  public TimeZone timeZoneOrParent() { return orElse(timeZone, safe(parent, JSONCoderOption::getTimeZone)); }

  // For performance reason, we need to cache SimpleDateFormat in the same thread as SimpleDateFormat is not threadsafe
  private static final InjectableFactory._2<String, TimeZone, SimpleDateFormat> dateFormatCache =
      InjectableFactory._2.of(JSONCoderOption::buildDateFormat, CacheThreadLocal.get());

  public SimpleDateFormat getCachedParsingDateFormat() { return getCachedDateFormat(parsingDateFormat); }
  public SimpleDateFormat getCachedDateFormat() { return getCachedDateFormat(dateFormat); }
  public SimpleDateFormat getCachedDateFormat(String format) { return dateFormatCache.get(format, timeZoneOrParent()); }


  private static SimpleDateFormat buildDateFormat(String format, TimeZone timeZone) {
    SimpleDateFormat result = new SimpleDateFormat(format);
    if (timeZone != null)
      result.setTimeZone(timeZone);
    return result;
  }

  /** Internal use only */

  @Getter private final List<String> fallbackDateFormats = new ArrayList<>();
  
  @Getter @Setter boolean alwaysMapKeyAsString;
  
  /**
   * Used by de-serializer, indicate throw exception if there's unknown field.
   */
  @Getter @Setter boolean errorOnUnknownProperty;
  
  @Getter private final Set<Class<?>> skippedClasses = new HashSet<>();
  
  //The class in this list, subclass field will be ignored.
  @Getter private final Set<Class<?>> ignoreSubClassFieldsClasses = new HashSet<>();


  @Getter private final List<Pair<Class<?>, FieldTransformer>> filters = new ArrayList<>();
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
  @Getter @Setter private TDJSONOption jsonOption = new TDJSONOption();

  public enum LogLevel {
    OFF { public void log(Logger log, String msg, Exception e) { /* Noop */ }},
    DEBUG { public void log(Logger log, String msg, Exception e) { log.debug(msg, e); }},
    INFO { public void log(Logger log, String msg, Exception e) { log.info(msg, e); }},
    WARN { public void log(Logger log, String msg, Exception e) { log.warn(msg, e); }},
    ERROR { public void log(Logger log, String msg, Exception e) { log.error(msg, e); }},
    ;
    public abstract void log(Logger logger, String message, Exception e);
  }
  @Getter @Setter private LogLevel warnLogLevel = LogLevel.INFO;

  /**
   * Accept specified sub-class using `$type` attribute. This feature is disabled by default for security reason
   */
  @Getter @Setter private boolean allowPolymorphicClasses = false;

  /**
   * Merge array. By default, when decode to exiting object, array or collection will be override instead of merge.
   * If this set true, it will merge the array (concatenation)
   */
  @Getter @Setter private boolean mergeArray = false;

  public JSONCoderOption() { this(global); }
  private JSONCoderOption(JSONCoderOption parent) { this.parent = parent; }
  public static JSONCoderOption of() { return new JSONCoderOption(); }
  public static JSONCoderOption ofIndentFactor(int factor) {
    return new JSONCoderOption().setJsonOption(TDJSONOption.ofIndentFactor(factor));
  }
  
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
  
  public FieldTransformer.FieldInfo transformField(
      Class<?> cls, Object o, BeanProperty property, BeanCoderContext beanCoderContext) {
    for (Pair<Class<?>, FieldTransformer> filter : filters) {
      if (!filter._1.isAssignableFrom(cls))
        continue;
      FieldTransformer.FieldInfo fieldInfo = filter._2.apply(o, property, beanCoderContext);
      if (fieldInfo != null)
        return fieldInfo;
    }
    
    return parent == null ? null : parent.transformField(cls, o, property, beanCoderContext);
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
        return getCachedDateFormat(fmt).parse(dateStr);
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
    if(ignoreSubClassFields)
      return true;
    for(Class iCls : ignoreSubClassFieldsClasses)
      if(iCls.isAssignableFrom(cls))
        return true;
    return parent != null && parent.isIgnoreSubClassFields(cls);
  }
  
  public SimpleFilter getDefaultFilter() { return getSimpleFilterFor(Object.class); }
  public SimpleFilter getSimpleFilterFor(Class<?> cls) {
    for (Pair<Class<?>, FieldTransformer> filter : filters) {
      if (!(filter._2 instanceof SimpleFilter) || filter._1 != cls)
        continue;
      return (SimpleFilter) filter._2;
    }
    SimpleFilter result = SimpleFilter.of();
    filters.add(0, Tuple.of(cls, result));
    return result;
  }

  public JSONCoderOption addFilterFor(Class<?> cls, FieldTransformer filter) {
    return addFilterFor(cls, filter, false);
  }

  public JSONCoderOption addFilterFor(Class<?> cls, FieldTransformer filter, boolean last) {
    Pair<Class<?>, FieldTransformer> clsToFilter = Tuple.of(cls, filter);
    doIfElse(last, () -> filters.add(clsToFilter), () -> filters.add(0, clsToFilter));
    return this;
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
