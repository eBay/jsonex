package com.jsonex.core.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;

/**
 * Field represent a java bean's field, with name, getter, mainly for functional style bean list/map transformation and filtering
 */
@RequiredArgsConstructor @Getter @ToString
public class BeanField<TBean, TField> implements Function<TBean, TField> {
  private final String name;
  private final Function<TBean, TField> getter;

  @Override public TField apply(TBean tBean) { return getter.apply(tBean); }

  public static <TBean, TField> BeanField<TBean, TField> of(String name, final Function<TBean, TField> getter) {
    return new BeanField<>(name, getter);
  }
}
