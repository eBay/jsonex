package com.jsonex.treedoc.json;

import com.google.gson.Gson;

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
    // Can't use JSONCoder due to cyclic dependencies.
    // return JSONCoder.getGlobal().encode(obj);
    // GSON cause stack overflow due to cyclic reference.
    // return new Gson().toJson(obj);
    return obj.toString();
  }
}
