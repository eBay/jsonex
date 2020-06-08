package com.jsonex.jsoncoder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class TestUtil {
  public static Reader loadResource(Class cls, String fileName) {
    InputStream in = cls.getResourceAsStream(fileName);
    if (in == null)
      throw new RuntimeException("Resource is not round: fileName: " + fileName + ", cls:" + cls.getCanonicalName());
    return new InputStreamReader(in);
  }

  public static String toJSON(Object obj) {
    return JSONCoder.getGlobal().encode(obj);
  }
}
