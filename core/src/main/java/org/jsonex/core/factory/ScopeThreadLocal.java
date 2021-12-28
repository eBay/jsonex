package org.jsonex.core.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScopeThreadLocal<T> implements ScopeProvider<T> {
  public final static InjectableInstance<ScopeThreadLocal> it = InjectableInstance.of(ScopeThreadLocal.class);
  public static <T> ScopeThreadLocal<T> get() { return it.get(); }

  // Seems ThreadLocal.withInitial() is not synchronized when create initial, so potentially it could be called multiple times in race condition
  private ThreadLocal<Map<Object, Scope>> cache = ThreadLocal.withInitial(ConcurrentHashMap::new);

  @Override public Scope getCache(Object scope) {
    return cache.get().computeIfAbsent(scope, (k) -> new ScopeMapImpl(new ConcurrentHashMap<>()));
  }
}
