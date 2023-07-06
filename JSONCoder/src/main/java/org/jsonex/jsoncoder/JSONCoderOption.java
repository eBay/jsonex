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
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.factory.InjectableFactory;
import org.jsonex.core.factory.ScopeThreadLocal;
import org.jsonex.core.type.Tuple;
import org.jsonex.core.type.Tuple.Pair;
import org.jsonex.core.type.Union.Union2;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.jsoncoder.coder.*;
import org.jsonex.jsoncoder.fieldTransformer.FieldTransformer;
import org.jsonex.jsoncoder.fieldTransformer.FieldTransformer.FieldInfo;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.json.TDJSONOption;
import org.slf4j.Logger;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.jsonex.core.util.LangUtil.*;
import static org.jsonex.core.util.ListUtil.listOf;
import static org.jsonex.jsoncoder.fieldTransformer.FieldTransformer.exclude;

@SuppressWarnings("UnusedReturnValue")
@Accessors(chain=true) @Slf4j
public class JSONCoderOption {
  @Getter final static JSONCoderOption global = new JSONCoderOption(null);
  static {
    global.addCoder(CoderDate.get(), CoderEnum.get(), CoderXMLGregorianCalendar.get(), CoderAtomicInteger.get(),
        CoderAtomicBoolean.get(), CoderBigInteger.get(), CoderClass.get(), CoderURI.get(), CoderURL.get());

    global.addSkippedClasses(Format.class, ClassLoader.class);
    global.fallbackDateFormats.add("yyyy-MM-dd HH:mm:ss.SSS.Z");  //Just for backward compatibility.
    global.fallbackDateFormats.add("yyyy/MM/dd HH:mm:ss.SSS.Z");
    global.fallbackDateFormats.add("yyyy-MM-dd HH:mm:ss.SSS");
    global.fallbackDateFormats.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
    global.fallbackDateFormats.add("yyyy-MM-dd HH:mm:ss");
    global.fallbackDateFormats.add("yyyy-MM-dd");
    global.fallbackDateFormats.add("HH:mm:ss");
    
    global
        .addDefaultFilter(exclude("copy"))  // DAO class has a getCopy() method
        .addFilterFor(AtomicReference.class, exclude("acquire", "opaque", "plain"));
  }
  private final JSONCoderOption parent;

  @Getter @Setter int maxObjects = 10_000;
  @Getter @Setter int maxDepth = 30;
  @Getter @Setter int maxElementsPerNode = 2000;

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
      InjectableFactory._2.of(JSONCoderOption::buildDateFormat, ScopeThreadLocal.get());

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
  @Getter private final Set<String> skippedPackages = new HashSet<>();
  
  //The class in this list, subclass field will be ignored.
  @Getter private final Set<Class<?>> ignoreSubClassFieldsClasses = new HashSet<>();


  @Getter private final List<Pair<Class<?>, FieldTransformer>> filters = new ArrayList<>();
  @Getter private final List<Pair<Union2<Class<?>, String>, FieldSelectOption>> fieldSelectOptions = new ArrayList<>();
  @Getter FieldSelectOption defaultFieldSelectOption = new FieldSelectOption();
  @Getter private final List<ICoder<?>> coderList = new ArrayList<>();
  
  /**
   * used to override equals and hashCode methods for certain object
   * e.g. BO object as there are no proper implementation of equals and hashCode 
   * which could cause duplicated copy of Object to be output.
   * 
   * The priority is based on the index of the wrapper. So to add for the highest priority
   * it needs to use equalsWrapper.add(0, wrapper).
   * 
   */
  @Getter private final List<EqualsWrapper<?>> equalsWrapper = new ArrayList<>();
  
  // JSON coder config
  @Getter @Setter private TDJSONOption jsonOption = TDJSONOption.ofDefaultRootType(TDNode.Type.SIMPLE);

  public enum LogLevel {
    OFF { public void log(Logger log, String msg, Throwable e) { /* Noop */ }},
    DEBUG { public void log(Logger log, String msg, Throwable e) { log.debug(msg, e); }},
    INFO { public void log(Logger log, String msg, Throwable e) { log.info(msg, e); }},
    WARN { public void log(Logger log, String msg, Throwable e) { log.warn(msg, e); }},
    ERROR { public void log(Logger log, String msg, Throwable e) { log.error(msg, e); }},
    ;
    public abstract void log(Logger logger, String message, Throwable e);
  }
  @Getter @Setter private LogLevel warnLogLevel = LogLevel.INFO;

  /**
   * Accept specified subclass using `$type` attribute. This feature is disabled by default for security reason
   */
  @Getter @Setter private boolean allowPolymorphicClasses = false;

  /**
   * Merge array. By default, when decode to exiting object, array or collection will be overridden instead of merge.
   * If this set true, it will merge the array (concatenation)
   */
  @Getter @Setter private boolean mergeArray = false;

  /**
   * As Java Map and Set implementation, the order may not be strictly consistent cross JVM implementation
   * set this to true, it will sort the keys in a deterministic order
   */
  @Getter @Setter private boolean sortMapAndSet = false;

  /**
   * In JVM implementation, Object getter method iteration order is not deterministic. Set this to true, it will
   * order the object keys. It won't sort Map's key order, for map or set ordering, please use sortMapAndSet
   */
  @Getter @Setter private boolean sortObjectKeys = false;

  public JSONCoderOption() { this(global); }
  private JSONCoderOption(JSONCoderOption parent) { this.parent = parent; }
  public static JSONCoderOption of() { return new JSONCoderOption(); }
  public static JSONCoderOption ofIndentFactor(int factor) {
    return new JSONCoderOption().setJsonOption(TDJSONOption.ofIndentFactor(factor));
  }

  public JSONCoderOption setStrictOrdering(boolean value) {
    return setSortMapAndSet(value).setSortObjectKeys(value);
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

    for (String pkg : skippedPackages) {
      if (cls.getPackage() != null && cls.getPackage().getName().startsWith(pkg))
        return true;
    }
    
    return parent != null && parent.isClassSkipped(cls);
  }
  
  public FieldInfo transformField(Class<?> cls, FieldInfo fieldInfo, BeanCoderContext ctx) {
    for (Pair<Class<?>, FieldTransformer> filter : filters) {
      if (!filter._0.isAssignableFrom(cls))
        continue;
      // TODO: Fix when to stop the filter chain strategy
      fieldInfo = filter._1.apply(fieldInfo, ctx);
    }
    
    return parent == null ? fieldInfo : parent.transformField(cls, fieldInfo, ctx);
  }

  public boolean isExcluded(Class<?> cls, String name, BeanCoderContext ctx) {
    for (Pair<Class<?>, FieldTransformer> filter : filters) {
      if (!filter._0.isAssignableFrom(cls))
        continue;
      if (!filter._1.shouldInclude(name, ctx))
        return true;
    }
    return parent == null ? false : parent.isExcluded(cls, name, ctx);
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

  public JSONCoderOption addDefaultFilter(FieldTransformer filter) {
    return addFilterFor(Object.class, filter, false);
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

  public JSONCoderOption addSkippedClasses(String... cls) {
    for (String c : cls) {
      try {
        skippedClasses.add(ClassUtil.forName(c));
      } catch (Exception e) {
        log.error("addSkippedClasses: Error load class: " + c);
      }
    }
    return this;
  }

  public JSONCoderOption addSkippedPackages(String... pkgs) {
    skippedPackages.addAll(listOf(pkgs));
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

  public FieldSelectOption getFieldSelectOption(Class<?> cls) {
    for (Pair<Union2<Class<?>, String>, FieldSelectOption> opt : fieldSelectOptions) {
      if (matches(opt._0, cls))
        return opt._1;
    }
    return defaultFieldSelectOption;
  }

  private static boolean matches(Union2<Class<?>, String> key, Class<?> cls) {
    if (key._0 != null)
      return key._0.isAssignableFrom(cls);
    // Package
    String pkg = key._1;
    String clsPkg = cls.getPackage().getName();
    // TODO: optimize to avoid create lots of string object with substring
    return pkg.endsWith("*") ? clsPkg.startsWith(pkg.substring(0, pkg.length() - 1)) : clsPkg == pkg;
  }

  public JSONCoderOption addFieldSelectOptionFor(Class<?> cls, FieldSelectOption filter) {
    return addFieldSelectOptionFor(cls, filter, false);
  }

  public JSONCoderOption addFieldSelectOptionFor(Class<?> cls, FieldSelectOption opt, boolean last) {
    Pair<Union2<Class<?>, String>, FieldSelectOption> clsOpt = Pair.of(Union2.of_0(cls), opt);
    doIfElse(last, () -> fieldSelectOptions.add(clsOpt), () -> fieldSelectOptions.add(0, clsOpt));
    return this;
  }

  public JSONCoderOption addFieldSelectOptionForPackage(String pkg, FieldSelectOption filter) {
    return addFieldSelectOptionForPackage(pkg, filter, false);
  }

  public JSONCoderOption addFieldSelectOptionForPackage(String pkg, FieldSelectOption opt, boolean last) {
    Pair<Union2<Class<?>, String>, FieldSelectOption> clsOpt = Pair.of(Union2.of_1(pkg), opt);
    doIfElse(last, () -> fieldSelectOptions.add(clsOpt), () -> fieldSelectOptions.add(0, clsOpt));
    return this;
  }

  public JSONCoderOption setIgnoreReadOnly(boolean v) { defaultFieldSelectOption.setIgnoreReadOnly(v); return this;}
  public JSONCoderOption setShowPrivateField(boolean v) { defaultFieldSelectOption.setShowPrivateField(v); return this;}
  public JSONCoderOption setShowTransientField(boolean v) { defaultFieldSelectOption.setShowTransientField(v); return this;}
  public JSONCoderOption setIgnoreSubClassFields(boolean v) { defaultFieldSelectOption.setIgnoreSubClassFields(v); return this;}
}
