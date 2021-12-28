package org.jsonex.core.factory;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

public interface ScopeProvider<T> {
  Scope getCache(Object scope);

  interface Scope<K, V> {
    V get(K k, Function<K, V> creator);
    V put(K k, V v);
    void clear();
  }

  @RequiredArgsConstructor
  class ScopeMapImpl<K, V> implements Scope<K, V> {
    private final Map<K, V> map;

    @Override public V get(K k, Function<K, V> creator) { return map.computeIfAbsent(k, creator); }
    @Override public V put(K k, V v) { return map.put(k, v); }
    @Override public void clear() { map.clear(); }
  }

  class ScopePassThrough<K, V> implements Scope<K, V> {
    @Override public V get(K k, Function<K, V> creator) { return creator.apply(k); }
    @Override public V put(K k, V v) {
      throw new UnsupportedOperationException("put can't be called for Pass Through cache"); }
    @Override public void clear() { }
  }

  class NoCache<T> implements ScopeProvider<T> {
    private  static InjectableInstance<NoCache> it = InjectableInstance.of(NoCache.class);
    public static <T> NoCache<T> get() { return it.get(); }
    @Override public Scope getCache(Object scope) { return new ScopePassThrough<>(); }
  }
}
