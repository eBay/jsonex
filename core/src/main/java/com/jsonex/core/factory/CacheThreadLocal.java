package com.jsonex.core.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheThreadLocal<T> implements CacheProvider<T> {
  private  static InjectableInstance<CacheThreadLocal> it = InjectableInstance.of(CacheThreadLocal.class);
  public static <T> CacheThreadLocal<T> get() { return it.get(); }

  // Seems ThreadLocal.withInitial() is not synchronized when create initial, so potentially it could be called multiple times in race condition
  private ThreadLocal<Map<Object, Map<Object, T>>> cache = ThreadLocal.withInitial(ConcurrentHashMap::new);

  @Override public Map<Object, T> getCache(Object key) {
    return cache.get().computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());
  }
}
