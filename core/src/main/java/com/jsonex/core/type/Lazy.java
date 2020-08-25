package com.jsonex.core.type;

import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

import static com.jsonex.core.util.LangUtil.doIf;

@RequiredArgsConstructor
public class Lazy<T> {
  private T value;

  public T getOrCompute(Supplier<T> supplier) {
    if (value == null) {
      synchronized (this) {
        doIf(value == null, () -> value = supplier.get());
      }
    }
    return value;
  }

  public synchronized void clear() {
    value = null;
  }
}
