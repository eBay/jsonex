package org.jsonex.core.util;

import lombok.SneakyThrows;

import java.io.*;

public class FileUtil {
  private final static int BUFFER_SIZE = 4096;
  public static Reader loadResource(Class cls, String fileName) {
    InputStream in = cls.getResourceAsStream(fileName);
    if (in == null)
      throw new RuntimeException("Resource is not found: fileName: " + fileName + ", cls:" + cls.getCanonicalName());
    return new InputStreamReader(in);
  }

  public static String readResource(Class cls, String fileName) {
    Reader reader = loadResource(cls, fileName);
    Assert.isNotNull(reader, () -> "Resource can't be loaded: cls:" + cls.getCanonicalName() + "; fileName:" + fileName);
    return read(reader);
  }

  public static String readFile(String file) { return readFile(new File(file)); }
  @SneakyThrows
  public static String readFile(File file) {
    try(Reader reader = new FileReader(file)) {
      return read(reader);
    }
  }

  public static void writeFile(String fileName, String content) { writeFile(fileName, content, false); }
  public static void writeFile(String fileName, String content, boolean append) {
    writeFile(new File(fileName), content, append);
  }

  public static void writeFile(File file, String content) { writeFile(file, content, false); }

  @SneakyThrows
  public static void writeFile(File file, String content, boolean append) {
    File parent = file.getParentFile();
    if (!parent.exists())
      if (!file.getParentFile().mkdirs())
        throw new IOException("Folder can't be created:" + parent);
    try(Writer writer = new FileWriter(file, append)) {
      writer.write(content);
    }
  }

  @SneakyThrows
  public static String read(Reader reader) {
    char[] buffer = new char[BUFFER_SIZE];
    StringBuffer sb = new StringBuffer();
    int size = 0;
    while ((size = reader.read(buffer)) > 0)
      sb.append(buffer, 0, size);
    return sb.toString();
  }

  public static void copyFrom(String outputFile, String... fileNames) {
    File[] files = new File[fileNames.length];
    for (int i = 0; i < files.length; i++)
      files[i] = new File(fileNames[i]);
    copyFrom(new File(outputFile), files);
  }

  @SneakyThrows
  public static void copyFrom(File outputFile, File... files) {
    try(OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
      copyFrom(out, files);
    }

    if (files.length == 1)  //Single file copy, reset the modified date.
      outputFile.setLastModified(files[0].lastModified());
  }

  public static void copyFrom(OutputStream out, File... fileNames) throws IOException {
    for (int i = 0; i < fileNames.length; i++)
      try(InputStream in = new FileInputStream(fileNames[i])) {
        copyFrom(out, in);
      }
  }

  public static void copyFrom(File outFile, InputStream in) throws IOException {
    try(OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
      copyFrom(out, in);
    }
  }

  @SneakyThrows
  public static void copyFrom(OutputStream out, InputStream in) {
    if (!(in instanceof BufferedInputStream))
      in = new BufferedInputStream(in);
    byte[] buffer = new byte[0x10000];
    int size;
    while ((size = in.read(buffer)) > 0)
      out.write(buffer, 0, size);
  }
}
