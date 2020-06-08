/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.factory;

import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/*
 * Injectable factory. Mainly for testing. it provides a convenient method for testing code to
 * replace object creation logic with mock implementation.
 * 
 * It supports different cache policies.
 */
@Accessors(chain = true)
public class InjectableFactory<TP, TI> {
  public enum CacheScope {
    NO_CACHE, THREAD_LOCAL, GLOBAL,
  }

  private Class<? extends TI> implClass;
  private Function<? super TP, ? extends TI> objectCreator;
  private final CacheScope cacheScope;
  
  private Map<Object, TI> globalCache;
  // Seems ThreadLocal.withInitial() is not synchronized when create initial, so potentially it could be called multiple times in race condition
  private final ThreadLocal<Map<Object, TI>> threadLocalCache = ThreadLocal.withInitial(ConcurrentHashMap::new);
  private final Object initialCreator;

  private InjectableFactory(Object creator, CacheScope cacheScope) {
    initialCreator = creator;
    this.cacheScope = cacheScope;
    setCreator(creator);
  }

  private InjectableFactory<TP, TI> setCreator(Object creator) {
    implClass = null;
    objectCreator = null;
    if (creator instanceof Class)
      implClass = (Class<? extends TI>)creator;
    else
      objectCreator = (Function<? super TP, ? extends TI>)creator;
    return clearCache();
  }

  public static <TI, TC extends TI> InjectableFactory<Void, TI> of(Class<TC> implCls) {
    return of(implCls, CacheScope.NO_CACHE);
  }
  
  public static <TP, TI, TC extends TI> InjectableFactory<TP, TI> of(Class<TP> paramCls, Class<TC> implCls) {
    return of(paramCls, implCls, CacheScope.NO_CACHE);
  }

  public static <TI, TC extends TI> InjectableFactory<Void, TI> of(Class<TC> implCls, CacheScope cacheScope) {
    return of(Void.class, implCls, cacheScope);
  }
  
  public static <TP, TI, TC extends TI> InjectableFactory<TP, TI> of(Class<TP> paramCls, Class<TC> implCls, CacheScope cacheScope) {
    if (implCls.isInterface() || Modifier.isAbstract(implCls.getModifiers()))
      throw new IllegalArgumentException("Implementation class has to be concrete class");
    return new InjectableFactory<>(implCls, cacheScope);
  }

  
  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator) {
    return of(objectCreator, CacheScope.NO_CACHE);
  }
  
  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator, CacheScope cacheScope) {
    return new InjectableFactory<>(objectCreator, cacheScope);
  }
  
  
  public TI get() { return get(null); }
  
  private Map<Object, TI> getCache() {
    switch(cacheScope) {
    case GLOBAL:
      if (globalCache == null) {
        synchronized (this) {
          if (globalCache == null)  // Double check in sync block
            globalCache = new ConcurrentHashMap<>();
        }
      }
      return globalCache;
    case THREAD_LOCAL:
      return threadLocalCache.get();
    default:
      return null;
    }
  }
  
  public TI get(TP param) {
    Map<Object, TI> cache = getCache();
    if (cache == null)
      return create(param);  // Not cached, create every time
    
    return cache.computeIfAbsent(getCacheKey(param), (key) -> create(param));
  }

  // Have to use a placeholder for null for ConcurrentHashMap unfortunately
  private Object getCacheKey(TP param) { return param == null ? Void.TYPE : param; }


  public <TC extends TI> InjectableFactory<TP, TI> setImplClass(Class<TC> implClass) { return setCreator(implClass); }
  public InjectableFactory<TP, TI> setObjectCreator(Function<TP, TI> objectCreator) { return setCreator(objectCreator); }
  public InjectableFactory<TP, TI> reset() { return setCreator(initialCreator); }

  /** Only Cached factory can call this method, otherwise, InvalidStateException will be thrown */
  public InjectableFactory<TP, TI> setInstance(TP param, TI instance) {
    Map<Object, TI> cache = getCache();
    if (cache == null)
      throw new IllegalStateException("setInstance can only be called for cached factory");
    cache.put(getCacheKey(param), instance);
    return this;
  }

  public InjectableFactory<TP, TI> setInstance(TI instance) { return setInstance(null, instance); }


  public InjectableFactory<TP, TI> clearCache() {
    if (threadLocalCache.get() != null)
      threadLocalCache.get().clear();
    if (globalCache != null)
      globalCache.clear();
    return this;
  }
  
  @SuppressWarnings("unchecked")
  @SneakyThrows
  private TI create(TP param) {
    if (objectCreator != null)
      return objectCreator.apply(param);
    else
      return param == null ? (TI) implClass.newInstance() :
          (TI)(implClass.getConstructor(param.getClass()).newInstance(param));
  }
}
