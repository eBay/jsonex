package com.jsonex.core.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheGlobal<T> implements CacheProvider<T> {
  private static InjectableInstance<CacheGlobal> it = InjectableInstance.of(CacheGlobal.class);
  public static <T> CacheGlobal<T> get() { return it.get(); }

  private final Map<Object, Map<Object, T>> cache = new ConcurrentHashMap<>();

  @Override public Map<Object, T> getCache(Object key) {
    return cache.computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());
  }
}
