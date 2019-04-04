/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.factory;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.experimental.Accessors;

/*
 * Injectable factory. Mainly for unit testing. provide a convenient method for testing code to
 * replace object creation logic with mock implementation.
 * 
 * It supports different cache policies.
 * 
 * @author jianwu chen
 */
@Accessors(chain = true)
public class InjectableFactory<TP, TI> {
  public enum CachePolicy {
    NO_CACHE, THREAD_LOCAL, GLOBAL,
  }
  // @VisibleForTesting 
  Class<?> implClass;
  // @VisibleForTesting
  Function<TP, TI> objectCreator;
  final CachePolicy cachePolicy;
  
  private Map<Object, TI> globalCache;
  private final ThreadLocal<Map<Object, TI>> threadLocalCache = new ThreadLocal<>();
  
  private InjectableFactory(Class<?> implCls, CachePolicy cachePolicy) {
    this.implClass = implCls;
    this.cachePolicy = cachePolicy;
  }
  
  private InjectableFactory(Function<TP, TI> objectCreator, CachePolicy cachePolicy) {
    this.objectCreator = objectCreator;
    this.cachePolicy = cachePolicy;
  }
  
  public static <TI, TC extends TI> InjectableFactory<Void, TI> of(Class<TC> implCls) {
    return of(implCls, CachePolicy.NO_CACHE);
  }
  
  public static <TP, TI, TC extends TI> InjectableFactory<TP, TI> of(Class<TP> paramCls, Class<TC> implCls) {
    return of(paramCls, implCls, CachePolicy.NO_CACHE);
  }

  public static <TI, TC extends TI> InjectableFactory<Void, TI> of(Class<TC> implCls, CachePolicy cachePolicy) {
    return of(Void.class, implCls, cachePolicy);
  }
  
  public static <TP, TI, TC extends TI> InjectableFactory<TP, TI> of(Class<TP> paramCls, Class<TC> implCls, CachePolicy cachePolicy) {
    if (implCls.isInterface() || Modifier.isAbstract(implCls.getModifiers()))
      throw new IllegalArgumentException("Implementation class has to be concrete class");
    return new InjectableFactory<>(implCls, cachePolicy);
  }

  
  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator) {
    return of(objectCreator, CachePolicy.NO_CACHE);
  }
  
  public static <TP, TI> InjectableFactory<TP, TI> of(Function<TP, TI> objectCreator, CachePolicy cachePolicy) {
    return new InjectableFactory<>(objectCreator, cachePolicy);
  }
  
  
  public TI get() { return get(null); }
  
  private Map<Object, TI> getCache() {
    switch(cachePolicy) { 
    case GLOBAL:
      if (globalCache == null) {
        globalCache = new ConcurrentHashMap<>();
      }
      return globalCache;
    case THREAD_LOCAL:
      Map<Object, TI> cache = threadLocalCache.get();
      if (cache == null) {
        cache = new ConcurrentHashMap<>();
        threadLocalCache.set(cache);
      }
      return cache;
    default:
      return null;
    }
  }
  
  public TI get(TP param) {
    Map<Object, TI> cache = getCache();
    if (cache == null)
      return create(param);
    
    Object cacheKey = param == null ? Void.TYPE : param;  // Have to use a placeholder for null for ConcurrentHashmap unfortunately
    TI result = cache.get(cacheKey);
    if (result == null) {
      result = create(param);
      cache.put(cacheKey, result);
    }
    return result;
  }
  
  @SuppressWarnings("UnusedReturnValue")
  public <TC extends TI> InjectableFactory<TP, TI> setImplClass(Class<TC> implClass) {
    this.implClass = implClass; 
    this.objectCreator = null; 
    return clearCache(); 
  }
  
  public InjectableFactory<TP, TI> setObjectCreator(Function<TP, TI> objectCreator) {
    this.objectCreator = objectCreator;
    this.implClass = null;
    return clearCache();
  }
  
  public InjectableFactory<TP, TI> clearCache() {
    if (threadLocalCache.get() != null)
      threadLocalCache.get().clear();
    if (globalCache != null)
      globalCache.clear();
    return this;
  }
  
  @SuppressWarnings("unchecked")
  private TI create(TP param) {
    try {
      if (objectCreator != null)
        return objectCreator.apply(param);
      else
        return param == null ? (TI) implClass.newInstance() :
            (TI)(implClass.getConstructor(param.getClass()).newInstance(param));
    } catch (Exception e) {
      throw new RuntimeException("Error creating default instance: " + e.toString(), e);
    }
  }
}
