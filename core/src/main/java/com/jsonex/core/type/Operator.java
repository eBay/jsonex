package com.jsonex.core.type;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.jsonex.core.util.LangUtil.safe;

/**
 * Similar to operators defined in java8 Predicate. Provides operator similar as SQL where clause that can be used in functional style
 * transformation and filtering.
 */
@UtilityClass
public class Operator {
  public <T> Predicate<T> and(Predicate<? super T> p1, Predicate<? super T> p2) { return and(new Predicate[]{p1, p2}); }
  public <T> Predicate<T> and(Predicate<? super T>... preds) {
    return obj -> {
      for (Predicate<? super T> p : preds) {
        if (!p.test(obj))
          return false;
      }
      return true;
    };
  }

  public <T> Predicate<T> or(Predicate<? super T> p1, Predicate<? super T> p2) { return or(new Predicate[]{p1, p2}); }
  public <T> Predicate<T> or(Predicate<? super T>... preds) {
    return obj -> {
      for (Predicate<? super T> p : preds) {
        if (p.test(obj))
          return true;
      }
      return false;
    };
  }

  public <T> Predicate<T> not(Predicate<? super T> p1) { return obj -> !p1.test(obj); }

  public <TBean, TField> Predicate<TBean> eq(Function<? super TBean, ? extends TField> getter, TField value) {
    return obj -> Objects.equals(getter.apply(obj), value);
  }

  public static <TBean, TField> Predicate<TBean> in(Function<? super TBean, ? extends TField> getter, TField... values) {
    return obj -> Arrays.asList(values).contains(getter.apply(obj));
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> gt(
      Function<? super TBean, ? extends TField> getter, TField value) {
    return obj -> isCompared(getter.apply(obj), value, true);
  }

  private static <T extends Comparable<? super T>> boolean isCompared(T v1, T v2, boolean isGreater) {
    if (v1 == null || v2 == null || v1.equals(v2))
      return false;

    return v1.compareTo(v2) > 0 == isGreater;
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> ge(
      Function<? super TBean, ? extends TField> getter, TField value) {
    return or(gt(getter, value), eq(getter, value));
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> lt(Function<? super TBean, ? extends TField> getter, TField value) {
    return obj -> isCompared(getter.apply(obj), value, false);
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> le(Function<? super TBean, ? extends TField> getter, TField value) {
    return or(lt(getter, value), eq(getter, value));
  }

  /** Compose methods are null-safe. i.e. It won't throw NPE if input is null, but just transform to null */
  public static<T, R1>  Function<T, R1> safeOf(Function<T, R1> f1) { return t -> safe(t, f1); }
  public static<T, R1, R2>  Function<T, R2> safeOf(Function<T, R1> f1, Function<R1, R2> f2) { return t -> safe(t, f1, f2); }
  public static <T, R1, R2, R3> Function<T, R3> safeOf(Function<T, R1> f1, Function<R1, R2> f2, Function<R2, R3> f3) { return t -> safe(t, f1, f2, f3); }
  public static <T, R1, R2, R3, R4> Function<T, R4> safeOf(Function<T, R1> f1, Function<R1, R2> f2, Function<R2, R3> f3, Function<R3, R4> f4) { return t -> safe(t, f1, f2, f3, f4); }
}
