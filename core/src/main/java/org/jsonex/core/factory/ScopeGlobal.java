package org.jsonex.core.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScopeGlobal<T> implements ScopeProvider<T> {
  public final static InjectableInstance<ScopeGlobal> it = InjectableInstance.of(ScopeGlobal.class);
  public static <T> ScopeGlobal<T> get() { return it.get(); }

  private final Map<Object, Scope> cache = new ConcurrentHashMap<>();

  @Override public Scope<Object, T> getCache(Object key) {
    return cache.computeIfAbsent(key, (k) -> new ScopeMapImpl(new ConcurrentHashMap<>()));
  }
}
