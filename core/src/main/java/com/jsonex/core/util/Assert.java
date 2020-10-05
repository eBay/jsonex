package com.jsonex.core.util;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class Assert {
  void isTrue(boolean condition, Supplier<String> error) { if (!condition) throw new AssertionError(error.get()); }
  void isNull(Object val, Supplier<String> error) { if (val != null) throw new AssertionError(error.get()); }
  void isNotNull(Object val, Supplier<String> error) { if (val == null) throw new AssertionError(error.get()); }
}
