/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.factory;

import org.jsonex.core.factory.ScopeProvider.NoCache;
import org.jsonex.core.factory.ScopeProvider.Scope;
import org.jsonex.core.type.Func;
import org.jsonex.core.type.Tuple;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
  private final ScopeProvider<TI> scopeProvider;
  
  private final Function<? super TP, ? extends TI> initialCreator;

  protected InjectableFactory(Function<? super TP, ? extends TI> creator, ScopeProvider<TI> scopeProvider) {
    initialCreator = creator;
    this.scopeProvider = scopeProvider;
    setCreator(creator);
  }

  protected InjectableFactory<TP, TI> setCreator(Function<? super TP, ? extends TI> creator) {
    objectCreator = creator;
    return clearCache();
  }
  
  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator) {
    return of(objectCreator, NoCache.get());
  }

  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator, ScopeProvider scopeProvider) {
    return new InjectableFactory<>(objectCreator, scopeProvider);
  }

  public TI get() { return get(null); }
  
  public TI get(TP param) {
    return getCache().get(getCacheKey(param), (key) -> create(param));
  }

  protected Scope<Object, TI> getCache() {
    return scopeProvider.getCache(this);
  }

  // Have to use a placeholder for null for ConcurrentHashMap unfortunately
  private Object getCacheKey(TP param) { return param == null ? Void.TYPE : param; }

  public InjectableFactory<TP, TI> setObjectCreator(Function<TP, TI> objectCreator) { return setCreator(objectCreator); }
  public InjectableFactory<TP, TI> reset() { return setCreator(initialCreator); }

  /** Only Cached factory can call this method, otherwise, InvalidStateException will be thrown */
  public InjectableFactory<TP, TI> setInstance(TP param, TI instance) {
    getCache().put(getCacheKey(param), instance);
    return this;
  }

  public InjectableFactory<TP, TI> setInstance(TI instance) { return setInstance(null, instance); }

  public InjectableFactory<TP, TI> clearCache() {
    getCache().clear();
    return this;
  }

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
    public _0(Function<Void, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <I> _0<I> of(Supplier<I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <I> _0<I> of(Supplier<I> objectCreator, ScopeProvider scopeProvider) { return new _0<>((Function<Void, I>)(p -> objectCreator.get()), scopeProvider); }
    public I get() { return super.get(null); }
  }

  public static class _2<P0, P1, I> extends InjectableFactory<Tuple.Pair<P0, P1>, I> {
    public _2(Function<Tuple.Pair<P0, P1>, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <P0, P1, I> _2<P0, P1, I> of(BiFunction<P0, P1, I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <P0, P1, I> _2<P0, P1, I> of(BiFunction<P0, P1, I> objectCreator, ScopeProvider scopeProvider) { return new _2<>((Function<Tuple.Pair<P0, P1>, I>)(p -> objectCreator.apply(p._0, p._1)), scopeProvider); }
    public I get(P0 p0, P1 p1) { return super.get(Tuple.Pair.of(p0, p1)); }
  }

  public static class _3<P0, P1, P2, I> extends InjectableFactory<Tuple.Tuple3<P0, P1, P2>, I> {
    public _3(Function<Tuple.Tuple3<P0, P1, P2>, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <P0, P1, P2, I> _3<P0, P1, P2, I> of(Func._3<P0, P1, P2, I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <P0, P1, P2, I> _3<P0, P1, P2, I> of(Func._3<P0, P1, P2, I> objectCreator, ScopeProvider scopeProvider) { return new _3<>((Function<Tuple.Tuple3<P0, P1, P2>, I>)(p -> objectCreator.apply(p._0, p._1, p._2)), scopeProvider); }
    public I get(P0 p0, P1 p1, P2 p2) { return super.get(Tuple.Tuple3.of(p0, p1, p2)); }
  }

  public static class _4<P0, P1, P2, P3, I> extends InjectableFactory<Tuple.Tuple4<P0, P1, P2, P3>, I> {
    public _4(Function<Tuple.Tuple4<P0, P1, P2, P3>, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <P0, P1, P2, P3, I> _4<P0, P1, P2, P3, I> of(Func._4<P0, P1, P2, P3, I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <P0, P1, P2, P3, I> _4<P0, P1, P2, P3, I> of(Func._4<P0, P1, P2, P3, I> objectCreator, ScopeProvider scopeProvider) { return new _4<>((Function<Tuple.Tuple4<P0, P1, P2, P3>, I>)(p -> objectCreator.apply(p._0, p._1, p._2, p._3)), scopeProvider); }
    public I get(P0 p0, P1 p1, P2 p2, P3 p3) { return super.get(Tuple.Tuple4.of(p0, p1, p2, p3)); }
  }

  public static class _5<P0, P1, P2, P3, P4, I> extends InjectableFactory<Tuple.Tuple5<P0, P1, P2, P3, P4>, I> {
    public _5(Function<Tuple.Tuple5<P0, P1, P2, P3, P4>, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <P0, P1, P2, P3, P4, I> _5<P0, P1, P2, P3, P4, I> of(Func._5<P0, P1, P2, P3, P4, I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <P0, P1, P2, P3, P4, I> _5<P0, P1, P2, P3, P4, I> of(Func._5<P0, P1, P2, P3, P4, I> objectCreator, ScopeProvider scopeProvider) { return new _5<>((Function<Tuple.Tuple5<P0, P1, P2, P3, P4>, I>)(p -> objectCreator.apply(p._0, p._1, p._2, p._3, p._4)), scopeProvider); }
    public I get(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4) { return super.get(Tuple.Tuple5.of(p0, p1, p2, p3, p4)); }
  }

  public static class _6<P0, P1, P2, P3, P4, P5, I> extends InjectableFactory<Tuple.Tuple6<P0, P1, P2, P3, P4, P5>, I> {
    public _6(Function<Tuple.Tuple6<P0, P1, P2, P3, P4, P5>, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <P0, P1, P2, P3, P4, P5, I> _6<P0, P1, P2, P3, P4, P5, I> of(Func._6<P0, P1, P2, P3, P4, P5, I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <P0, P1, P2, P3, P4, P5, I> _6<P0, P1, P2, P3, P4, P5, I> of(Func._6<P0, P1, P2, P3, P4, P5, I> objectCreator, ScopeProvider scopeProvider) { return new _6<>((Function<Tuple.Tuple6<P0, P1, P2, P3, P4, P5>, I>)(p -> objectCreator.apply(p._0, p._1, p._2, p._3, p._4, p._5)), scopeProvider); }
    public I get(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) { return super.get(Tuple.Tuple6.of(p0, p1, p2, p3, p4,p5)); }
  }

  public static class _7<P0, P1, P2, P3, P4, P5, P6, I> extends InjectableFactory<Tuple.Tuple7<P0, P1, P2, P3, P4, P5, P6>, I> {
    public _7(Function<Tuple.Tuple7<P0, P1, P2, P3, P4, P5, P6>, I> creator, ScopeProvider scopeProvider) { super(creator, scopeProvider); }
    public static <P0, P1, P2, P3, P4, P5, P6, I> _7<P0, P1, P2, P3, P4, P5, P6, I> of(Func._7<P0, P1, P2, P3, P4, P5, P6, I> objectCreator) { return of(objectCreator, NoCache.get()); }
    public static <P0, P1, P2, P3, P4, P5, P6, I> _7<P0, P1, P2, P3, P4, P5, P6, I> of(Func._7<P0, P1, P2, P3, P4, P5, P6, I> objectCreator, ScopeProvider scopeProvider) { return new _7<>((Function<Tuple.Tuple7<P0, P1, P2, P3, P4, P5, P6>, I>)(p -> objectCreator.apply(p._0, p._1, p._2, p._3, p._4, p._5, p._6)), scopeProvider); }
    public I get(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) { return super.get(Tuple.Tuple7.of(p0, p1, p2, p3, p4, p5, p6)); }
  }
}
