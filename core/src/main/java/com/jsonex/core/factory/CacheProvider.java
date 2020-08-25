package com.jsonex.core.factory;

import java.util.Map;

public interface CacheProvider<T> {
  Map<Object, T> getCache(Object key);
}
