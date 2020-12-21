package com.jsonex.core.util;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class Assert {
  public void isTrue(boolean condition, Supplier<String> error) { if (!condition) throw new AssertionError(error.get()); }
  public void isNull(Object val, Supplier<String> error) { if (val != null) throw new AssertionError(error.get()); }
  public void isNotNull(Object val, Supplier<String> error) { if (val == null) throw new AssertionError(error.get()); }

  public void isTrue(boolean condition, String error) { if (!condition) throw new AssertionError(error); }
  public void isNull(Object val, String error) { if (val != null) throw new AssertionError(error); }
  public void isNotNull(Object val, String error) { if (val == null) throw new AssertionError(error); }
}
