package com.jsonex.core.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LangUtil {
  /**
   * Method extension utility to call getter avoid NPE if the obj is null. It simulates optional chain (?.)
   * in other languages
   */
  public static <T,R> R safe(T obj, Function<T,R> getter) { return obj == null ? null : getter.apply(obj); }
  public static <T,R1, R2> R2 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2) { return safe(safe(obj, getter1), getter2); }
  public static <T,R1, R2, R3> R3 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3) { return safe(safe(obj, getter1, getter2), getter3); }
  public static <T,R1, R2, R3, R4> R4 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Function<R3,R4> getter4) { return safe(safe(obj, getter1, getter2, getter3), getter4); }
  public static <T,R1, R2, R3, R4, R5> R5 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Function<R3,R4> getter4, Function<R4,R5> getter5) { return safe(safe(obj, getter1, getter2, getter3, getter4), getter5); }

  public static <T> void safeConsume(T obj, Consumer<T> action) { if (obj != null)  action.accept(obj); }

  public static void doIf(boolean condition, Runnable action) { if (condition) action.run(); }
  public static void doIfElse(boolean condition, Runnable ifAction, Runnable elseAction) {
    if (condition)
      ifAction.run();
    else
      elseAction.run();
  }

  public static <T> T orElse(T value, T fullBack) { return value == null ? fullBack : value; }
  public static <T> T orElse(T value, Supplier<T> fullBack) { return value == null ? fullBack.get() : value; }
}
