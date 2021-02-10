package org.jsonex.core.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheThreadLocal<T> implements CacheProvider<T> {
  public final static InjectableInstance<CacheThreadLocal> it = InjectableInstance.of(CacheThreadLocal.class);
  public static <T> CacheThreadLocal<T> get() { return it.get(); }

  // Seems ThreadLocal.withInitial() is not synchronized when create initial, so potentially it could be called multiple times in race condition
  private ThreadLocal<Map<Object, ObjectCache>> cache = ThreadLocal.withInitial(ConcurrentHashMap::new);

  @Override public ObjectCache getCache(Object scope) {
    return cache.get().computeIfAbsent(scope, (k) -> new ObjectCacheMapImpl(new ConcurrentHashMap<>()));
  }
}
