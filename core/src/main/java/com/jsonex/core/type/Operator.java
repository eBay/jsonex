package com.jsonex.core.type;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Similar to operators defined in java8 Predicate. Provides operator similar as SQL where clause that can be used in functional style
 * transformation and filtering.
 */
public class Operator {
  public static <T> Predicate<T> and(final Predicate<? super T> p1, final Predicate<? super T> p2) { return obj -> p1.test(obj) && p2.test(obj); }
  public static <T> Predicate<T> or(final Predicate<? super T> p1, final Predicate<? super T> p2) { return obj -> p1.test(obj) || p2.test(obj); }
  public static <T> Predicate<T> not(final Predicate<? super T> p1) { return obj -> !p1.test(obj); }

  public static <TBean, TField> Predicate<TBean> eq(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return obj -> Objects.equals(getter.apply(obj), value);
  }

  public static <TBean, TField> Predicate<TBean> in(final Function<? super TBean, ? extends TField> getter, final TField... values) {
    return obj -> Arrays.asList(values).contains(getter.apply(obj));
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> gt(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return obj -> isCompared(getter.apply(obj), value, true);
  }

  private static <T extends Comparable<? super T>> boolean isCompared(T v1, T v2, boolean isGreater) {
    if (v1 == null || v2 == null || v1.equals(v2))
      return false;

    return v1.compareTo(v2) > 0 == isGreater;
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> ge(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return or(gt(getter, value), eq(getter, value));
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> lt(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return obj -> isCompared(getter.apply(obj), value, false);
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> le(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return or(lt(getter, value), eq(getter, value));
  }
}
