package com.ebay.jsoncodercore.type;

import java.util.Objects;

/**
 * Similar to operators defined in java8 Predicate. Provides operator similar as SQL where clause that can be used in functional style
 * transformation and filtering.
 */
public class Operator {
  public static <T> Predicate<T> and(final Predicate<? super T> p1, final Predicate<? super T> p2) {
    return new Predicate<T>() {@Override public boolean test(T obj) { return p1.test(obj) && p2.test(obj); }};
  }

  public static <T> Predicate<T> or(final Predicate<? super T> p1, final Predicate<? super T> p2) {
    return new Predicate<T>() {@Override public boolean test(T obj) { return p1.test(obj) || p2.test(obj); }};
  }

  public static <T> Predicate<T> not(final Predicate<? super T> p1) {
    return new Predicate<T>() {@Override public boolean test(T obj) { return !p1.test(obj); }};
  }

  public static <TBean, TField> Predicate<TBean> eq(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return new Predicate<TBean>() {@Override public boolean test(TBean obj) {
      TField fVal = getter.apply(obj);
      if (value == null)
        return fVal == null;
      return value.equals(fVal);
    }};
  }

  public static <TBean, TField> Predicate<TBean> in(final Function<? super TBean, ? extends TField> getter, final TField... values) {
    return new Predicate<TBean>() {@Override public boolean test(TBean obj) {
      TField fVal = getter.apply(obj);
      for (TField v : values) {
        if (v.equals(fVal))
          return true;
      }
      return false;
    }};
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> gt(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return new Predicate<TBean>() {@Override public boolean test(TBean obj) {
      TField fVal = getter.apply(obj);
      if (value == null)
        return false;
      return fVal.compareTo(value) > 0;
    }};
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> ge(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return or(gt(getter, value), eq(getter, value));
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> le(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return not(gt(getter, value));
  }

  public static <TBean, TField extends Comparable<? super TField>> Predicate<TBean> lt(final Function<? super TBean, ? extends TField> getter, final TField value) {
    return not(ge(getter, value));
  }
}
