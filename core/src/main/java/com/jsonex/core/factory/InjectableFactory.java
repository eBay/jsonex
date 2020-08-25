/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.factory;

import com.jsonex.core.type.Func;
import com.jsonex.core.type.Tuple;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.jsonex.core.util.LangUtil.doIf;

/*
 * Injectable factory. Mainly for testing. it provides a convenient method for testing code to
 * replace object creation logic with mock implementation.
 * 
 * It supports different cache policies.
 */
@Accessors(chain = true)
public class InjectableFactory<TP, TI> {

  private Function<? super TP, ? extends TI> objectCreator;
  @Getter private static List<Function<Object, Object>> globalCreateHandlers = new ArrayList<>();
  @Getter private List<Function<TI, TI>> createHandlers = new ArrayList<>();
  private final @NonNull CacheProvider<TI> cacheProvider;
  
  private final Function<? super TP, ? extends TI> initialCreator;

  protected InjectableFactory(Function<? super TP, ? extends TI> creator, CacheProvider<TI> cacheProvider) {
    initialCreator = creator;
    this.cacheProvider = cacheProvider;
    setCreator(creator);
  }

  protected InjectableFactory<TP, TI> setCreator(Function<? super TP, ? extends TI> creator) {
    objectCreator = creator;
    return clearCache();
  }
  
  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator) {
    return of(objectCreator, null);
  }

  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator, CacheProvider cacheProvider) {
    return new InjectableFactory<>(objectCreator, cacheProvider);
  }
  
  
  public TI get() { return get(null); }
  
  public TI get(TP param) {
    if (cacheProvider == null)
      return create(param);  // Not cached, create every time

    return cacheProvider.getCache(this).computeIfAbsent(getCacheKey(param), (key) -> create(param));
  }

  // Have to use a placeholder for null for ConcurrentHashMap unfortunately
  private Object getCacheKey(TP param) { return param == null ? Void.TYPE : param; }


  public InjectableFactory<TP, TI> setObjectCreator(Function<TP, TI> objectCreator) { return setCreator(objectCreator); }
  public InjectableFactory<TP, TI> reset() { return setCreator(initialCreator); }

  /** Only Cached factory can call this method, otherwise, InvalidStateException will be thrown */
  public InjectableFactory<TP, TI> setInstance(TP param, TI instance) {
    if (cacheProvider == null)
      throw new IllegalStateException("setInstance can only be called for cached factory");
    cacheProvider.getCache(this).put(getCacheKey(param), instance);
    return this;
  }

  public InjectableFactory<TP, TI> setInstance(TI instance) { return setInstance(null, instance); }

  public InjectableFactory<TP, TI> clearCache() {
    doIf(cacheProvider != null , () -> cacheProvider.getCache(this).clear());
    return this;
  }
  
  @SneakyThrows
  private TI create(TP param) {
    TI obj = objectCreator.apply(param);
    for (Function<Object,Object> func : globalCreateHandlers)
      obj = (TI)func.apply(obj);
    for (Function<TI, TI> func : createHandlers) {
      obj = func.apply(obj);
    }
    return obj;
  }

  public static class _0<I> extends InjectableFactory<Void, I> {
    public _0(Function<Void, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <I> _0<I> of(Supplier<I> objectCreator) { return of(objectCreator, null); }
    public static <I> _0<I> of(Supplier<I> objectCreator, CacheProvider cacheProvider) { return new _0<>((Function<Void, I>)(p -> objectCreator.get()), cacheProvider); }
    public I get() { return super.get(null); }
  }

  public static class _2<P1, P2, I> extends InjectableFactory<Tuple._2<P1, P2>, I> {
    public _2(Function<Tuple._2<P1, P2>, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <P1, P2, I> _2<P1, P2, I> of(BiFunction<P1, P2, I> objectCreator) { return of(objectCreator, null); }
    public static <P1, P2, I> _2<P1, P2, I> of(BiFunction<P1, P2, I> objectCreator, CacheProvider cacheProvider) { return new _2<>((Function<Tuple._2<P1, P2>, I>)(p -> objectCreator.apply(p._1, p._2)), cacheProvider); }
    public I get(P1 p1, P2 p2) { return super.get(new Tuple._2<>(p1, p2)); }
  }

  public static class _3<P1, P2, P3, I> extends InjectableFactory<Tuple._3<P1, P2, P3>, I> {
    public _3(Function<Tuple._3<P1, P2, P3>, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <P1, P2, P3, I> _3<P1, P2, P3, I> of(Func._3<P1, P2, P3, I> objectCreator) { return of(objectCreator, null); }
    public static <P1, P2, P3, I> _3<P1, P2, P3, I> of(Func._3<P1, P2, P3, I> objectCreator, CacheProvider cacheProvider) { return new _3<>((Function<Tuple._3<P1, P2, P3>, I>)(p -> objectCreator.apply(p._1, p._2, p._3)), cacheProvider); }
    public I get(P1 p1, P2 p2, P3 p3) { return super.get(new Tuple._3<>(p1, p2, p3)); }
  }

  public static class _4<P1, P2, P3, P4, I> extends InjectableFactory<Tuple._4<P1, P2, P3, P4>, I> {
    public _4(Function<Tuple._4<P1, P2, P3, P4>, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <P1, P2, P3, P4, I> _4<P1, P2, P3, P4, I> of(Func._4<P1, P2, P3, P4, I> objectCreator) { return of(objectCreator, null); }
    public static <P1, P2, P3, P4, I> _4<P1, P2, P3, P4, I> of(Func._4<P1, P2, P3, P4, I> objectCreator, CacheProvider cacheProvider) { return new _4<>((Function<Tuple._4<P1, P2, P3, P4>, I>)(p -> objectCreator.apply(p._1, p._2, p._3, p._4)), cacheProvider); }
    public I get(P1 p1, P2 p2, P3 p3, P4 p4) { return super.get(new Tuple._4<>(p1, p2, p3, p4)); }
  }

  public static class _5<P1, P2, P3, P4, P5, I> extends InjectableFactory<Tuple._5<P1, P2, P3, P4, P5>, I> {
    public _5(Function<Tuple._5<P1, P2, P3, P4, P5>, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <P1, P2, P3, P4, P5, I> _5<P1, P2, P3, P4, P5, I> of(Func._5<P1, P2, P3, P4, P5, I> objectCreator) { return of(objectCreator, null); }
    public static <P1, P2, P3, P4, P5, I> _5<P1, P2, P3, P4, P5, I> of(Func._5<P1, P2, P3, P4, P5, I> objectCreator, CacheProvider cacheProvider) { return new _5<>((Function<Tuple._5<P1, P2, P3, P4, P5>, I>)(p -> objectCreator.apply(p._1, p._2, p._3, p._4, p._5)), cacheProvider); }
    public I get(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) { return super.get(new Tuple._5<>(p1, p2, p3, p4, p5)); }
  }

  public static class _6<P1, P2, P3, P4, P5, P6, I> extends InjectableFactory<Tuple._6<P1, P2, P3, P4, P5, P6>, I> {
    public _6(Function<Tuple._6<P1, P2, P3, P4, P5, P6>, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <P1, P2, P3, P4, P5, P6, I> _6<P1, P2, P3, P4, P5, P6, I> of(Func._6<P1, P2, P3, P4, P5, P6, I> objectCreator) { return of(objectCreator, null); }
    public static <P1, P2, P3, P4, P5, P6, I> _6<P1, P2, P3, P4, P5, P6, I> of(Func._6<P1, P2, P3, P4, P5, P6, I> objectCreator, CacheProvider cacheProvider) { return new _6<>((Function<Tuple._6<P1, P2, P3, P4, P5, P6>, I>)(p -> objectCreator.apply(p._1, p._2, p._3, p._4, p._5, p._6)), cacheProvider); }
    public I get(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) { return super.get(new Tuple._6<>(p1, p2, p3, p4, p5,p6)); }
  }

  public static class _7<P1, P2, P3, P4, P5, P6, P7, I> extends InjectableFactory<Tuple._7<P1, P2, P3, P4, P5, P6, P7>, I> {
    public _7(Function<Tuple._7<P1, P2, P3, P4, P5, P6, P7>, I> creator, CacheProvider cacheProvider) { super(creator, cacheProvider); }
    public static <P1, P2, P3, P4, P5, P6, P7, I> _7<P1, P2, P3, P4, P5, P6, P7, I> of(Func._7<P1, P2, P3, P4, P5, P6, P7, I> objectCreator) { return of(objectCreator, null); }
    public static <P1, P2, P3, P4, P5, P6, P7, I> _7<P1, P2, P3, P4, P5, P6, P7, I> of(Func._7<P1, P2, P3, P4, P5, P6, P7, I> objectCreator, CacheProvider cacheProvider) { return new _7<>((Function<Tuple._7<P1, P2, P3, P4, P5, P6, P7>, I>)(p -> objectCreator.apply(p._1, p._2, p._3, p._4, p._5, p._6, p._7)), cacheProvider); }
    public I get(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7) { return super.get(new Tuple._7<>(p1, p2, p3, p4, p5, p6, p7)); }
  }
}
