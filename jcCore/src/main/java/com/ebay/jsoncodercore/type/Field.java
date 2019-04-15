package com.ebay.jsoncodercore.type;

import lombok.RequiredArgsConstructor;

/**
 * Field represent a java bean's field, with name, getter and setting, mainly for functional style bean list/map transformation and filtering
 */
@RequiredArgsConstructor
public abstract class Field<TBean, TField> implements Function<TBean, TField> {
  private final String name;
}
