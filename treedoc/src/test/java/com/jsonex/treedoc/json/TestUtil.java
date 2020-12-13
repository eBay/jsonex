package com.jsonex.treedoc.json;

public class TestUtil {
  public static String toJSON(Object obj) {
    // Can't use JSONCoder due to circular dependencies.
    // return JSONCoder.getGlobal().encode(obj);
    // GSON cause stack overflow due to cyclic reference.
    // return new Gson().toJson(obj);
    return obj == null ? "null" : obj.toString();
  }
}
