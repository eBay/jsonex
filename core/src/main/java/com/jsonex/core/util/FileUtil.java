package com.jsonex.core.util;

import lombok.SneakyThrows;

import java.io.*;

public class FileUtil {
  private final static int BUFFER_SIZE = 4096;
  public static Reader loadResource(Class cls, String fileName) {
    InputStream in = cls.getResourceAsStream(fileName);
    if (in == null)
      throw new RuntimeException("Resource is not round: fileName: " + fileName + ", cls:" + cls.getCanonicalName());
    return new InputStreamReader(in);
  }

  public static String readResource(Class cls, String fileName) {
    Reader reader = loadResource(cls, fileName);
    Assert.isNotNull(reader, () -> "Resource can't be loaded: cls:" + cls.getCanonicalName() + "; fileName:" + fileName);
    return read(reader);
  }

  @SneakyThrows
  public static String readFile(String file) {
    try(Reader reader = new FileReader(file)) {
      return read(reader);
    }
  }

  @SneakyThrows
  public static String read(Reader reader) {
    char[] buffer = new char[BUFFER_SIZE];
    StringBuffer sb = new StringBuffer();
    int size = 0;
    while ((size = reader.read(buffer)) > 0) {
      sb.append(buffer, 0, size);
    }
    return sb.toString();
  }
}
