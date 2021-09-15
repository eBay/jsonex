package org.jsonex.jsoncoder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoneComparable {
  public final String str;

  @Override
  public String toString() {
    return "NoneComparable{str='" + str + '\'' + '}';
  }
}
