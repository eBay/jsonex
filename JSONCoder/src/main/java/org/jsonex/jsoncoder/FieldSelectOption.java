package org.jsonex.jsoncoder;

import lombok.Data;

@Data
public class FieldSelectOption {
  /** If true, when convert from a java bean, the readonly field will be ignored */
  boolean ignoreReadOnly;

  /** If true, subclass field won't be encoded */
  boolean ignoreSubClassFields;

  /** If true, for java bean type, only field include private will be returned, no setter getter method will be returned */
  boolean showPrivateField;

  /** by default, transientField won't be serialized. Set this to true will serialize it */
  boolean showTransientField;
}
