package org.jsonex.jsoncoder;

public class TestUtil {
  public static String toJSON(Object obj) {
    return JSONCoder.getGlobal().encode(obj);
  }
}
