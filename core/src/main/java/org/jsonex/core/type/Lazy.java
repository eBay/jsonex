package org.jsonex.core.type;

import lombok.RequiredArgsConstructor;
import org.jsonex.core.util.LangUtil;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class Lazy<T> {
  private T value;

  public T getOrCompute(Supplier<T> supplier) {
    if (value == null) {
      synchronized (this) {
        LangUtil.doIf(value == null, () -> value = supplier.get());
      }
    }
    return value;
  }

  public synchronized void clear() {
    value = null;
  }
}
