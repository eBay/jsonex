package org.jsonex.core.util;

import lombok.SneakyThrows;
import org.jsonex.core.type.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LangUtil {
  /**
   * Method extension utility to call getter avoid NPE if the obj is null. It simulates optional chain (?.)
   * in other languages
   */
  public static <T,R> R safeOrElse(T obj, Function<T,R> getter, @Nullable R def) { return orElse(obj == null ? null : getter.apply(obj), def); }
  public static <T,R> R safeOrElse(T obj, Function<T,R> getter, Supplier<R> def) { return orElse(obj == null ? null : getter.apply(obj), def); }
  public static <T,R> R safe(T obj, Function<T,R> getter) { return safeOrElse(obj, getter, (R) null); }
  public static <T,R1, R2> R2 safeOrElse(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, @Nullable R2 def) { return safeOrElse(safe(obj, getter1), getter2, def); }
  public static <T,R1, R2> R2 safeOrElse(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Supplier<R2> def) { return safeOrElse(safe(obj, getter1), getter2, def); }
  public static <T,R1, R2> R2 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2) { return safeOrElse(obj, getter1, getter2, (R2) null); }
  public static <T,R1, R2, R3> R3 safeOrElse(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, @Nullable R3 def) { return safeOrElse(safe(obj, getter1, getter2), getter3, def); }
  public static <T,R1, R2, R3> R3 safeOrElse(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Supplier<R3> def) { return safeOrElse(safe(obj, getter1, getter2), getter3, def); }
  public static <T,R1, R2, R3> R3 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3) { return safeOrElse(obj, getter1, getter2, getter3, (R3) null); }
  public static <T,R1, R2, R3, R4> R4 safeOrElse(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Function<R3,R4> getter4, R4 def) { return safeOrElse(safe(obj, getter1, getter2, getter3), getter4, def); }
  public static <T,R1, R2, R3, R4> R4 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Function<R3,R4> getter4) { return safeOrElse(obj, getter1, getter2, getter3, getter4, null); }
  public static <T,R1, R2, R3, R4, R5> R5 safeOrElse(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Function<R3,R4> getter4, Function<R4,R5> getter5, R5 def) { return safeOrElse(safe(obj, getter1, getter2, getter3, getter4), getter5, def); }

  public static <T,R1, R2, R3, R4, R5> R5 safe(T obj, Function<T,R1> getter1, Function<R1,R2> getter2, Function<R2,R3> getter3, Function<R3,R4> getter4, Function<R4,R5> getter5) { return safeOrElse(obj, getter1, getter2, getter3, getter4, getter5, null); }

  public static <T> void doIfNotNull(T obj, Consumer<? super T> action) { if (obj != null)  action.accept(obj); }
  public static <T> void doIfNotNullOrElse(T obj, Consumer<? super T> action, Runnable elseAction) {
    if (obj != null)
      action.accept(obj);
    else
      elseAction.run();
  }

  public static void doIf(boolean condition, Runnable action) { if (condition) action.run(); }
  public static void doIfElse(boolean condition, Runnable ifAction, Runnable elseAction) {
    if (condition)
      ifAction.run();
    else
      elseAction.run();
  }

  @SneakyThrows
  public static void throwIf(boolean condition, Supplier<Exception> exp) { if (condition) throw exp.get(); }

  public static <T> void doIfInstanceOf(Object obj, Class<T> cls, Consumer<? super T> action) {
    if (obj != null && cls.isAssignableFrom(obj.getClass()))
      action.accept(cls.cast(obj));
  }

  public static <T> void doIfInstanceOfOrElseThrow(Object obj, Class<T> cls, Consumer<? super T> action) {
    doIfInstanceOfOrElseThrow(obj, cls, action, () ->
        new IllegalStateException("Expect class: " + cls + ";got: " + safe(obj, Object::getClass)));
  }

  @SneakyThrows
  public static <T> void doIfInstanceOfOrElseThrow(Object obj, Class<T> cls, Consumer<? super T> action, Supplier<Exception> exp) {
    if (obj != null && cls.isAssignableFrom(obj.getClass()))
      action.accept(cls.cast(obj));
    else
      throw exp.get();
  }

  public static <TO, TC, R> R getIfInstanceOf(
      TO obj, Class<TC> cls, Function<? super TC, ? extends R> func, Function<TO, ? extends R> elseFunc) {
    return obj != null && cls.isAssignableFrom(obj.getClass()) ? func.apply(cls.cast(obj)) : elseFunc.apply(obj);
  }

  public static <T, R> R getIfInstanceOfElseThrow(
      Object obj, Class<T> cls, Function<? super T, ? extends R> func) {
    return getIfInstanceOfElseThrow(obj, cls, func, () ->
        new IllegalStateException("Expect class: " + cls + ";got: " + safe(obj, Object::getClass)));
  }

  @SneakyThrows
  public static <T, R> R getIfInstanceOfElseThrow(
      Object obj, Class<T> cls, Function<? super T, ? extends R> func, Supplier<Exception> exp) {
    if (obj != null && cls.isAssignableFrom(obj.getClass()))
      return func.apply(cls.cast(obj));
    throw exp.get();
  }

  public static <T, T1 extends T, T2 extends T> T orElse(T1 value, T2 fullBack) {
    return value == null ? fullBack : value;
  }

  public static <T, T1 extends T> T orElse(T1 value, Supplier<? extends T> fullBack) {
    return value == null ? fullBack.get() : value;
  }
}
