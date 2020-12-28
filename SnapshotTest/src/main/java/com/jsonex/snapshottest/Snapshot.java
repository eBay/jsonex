package com.jsonex.snapshottest;

import com.jsonex.core.type.Lazy;
import com.jsonex.core.util.FileUtil;
import com.jsonex.core.util.ClassUtil;
import com.jsonex.jsoncoder.JSONCoder;
import com.jsonex.jsoncoder.JSONCoderOption;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

import static com.jsonex.core.util.LangUtil.getIfInstanceOf;

/**
 * Snapshot is a simple test utility to record actual output into snapshot file when the test is first time run,
 * and for subsequent runs, it will match the new output against the recorded snapshot value.
 * To approve the expected changes, just rename the newly generated tmp file to the original snapshot file.
 * By default, the snapshot files and the tmp files are stored in the src/test/resources/[package name]/__snapshot__.
 */
@Data @RequiredArgsConstructor @Slf4j
public class Snapshot {
  enum Result { MATCHES, MISMATCH, INITIAL }
  private final static String TEST_RESOURCE_ROOT = "src/test/resources";

  private final String testClass;
  private final String testMethod;
  private final String name;
  private final Object actual;
  private final Lazy<String> actualString = new Lazy<>();
  private String expected;

  private Result result;  // For testing only
  private String message;  // For testing only

  public String getFile() {
    int packagePos = testClass.lastIndexOf(".");
    String baseName = testClass.substring(0, packagePos) + ".__snapshot__" +  testClass.substring(packagePos);
    String ext = actual instanceof String ? ".txt" : ".json";
    return TEST_RESOURCE_ROOT + "/" +
        baseName.replace(".", "/") + "_" + testMethod + "_" + name + ext;
  }

  public String getTempFile() { return getFile() + ".tmp"; }

  public String getActualString() {
    return actualString.getOrCompute(() ->
        getIfInstanceOf(actual, String.class, Function.identity(), Snapshot::toJson));
  }

  private static String toJson(Object obj) { return JSONCoder.encode(obj, JSONCoderOption.ofIndentFactor(2)); }

  /** @return true if snapshot exists and matches*/
  public Snapshot compareOrRecord() {
    File file = new File(getFile());
    String actualString = getActualString();
    File tempFile = new File(getTempFile());
    if (file.exists()) {
      String expected = FileUtil.readFile(file);
      if (Objects.equals(actualString, expected)) {
        tempFile.delete();
        result = Result.MATCHES;
        message = null;
        return this;
      }
      message = this + " and actual mismatch. The actual is record in file:\nfile://" + tempFile.getAbsolutePath() +
          "\nPlease review the difference. If the change is expected, please override the snapshot file with the tmp file.";
      log.error(message);
      FileUtil.writeFile(tempFile, actualString);
      result = Result.MISMATCH;
      throw new AssertionError(this + " and actual mismatch " + "expected:<" + expected + "> but was:<" + getActualString() + ">");
    } else {
      message = this + " doesn't exist, will record the initial snapshot in\n file://" + file.getAbsolutePath()
          + "\n snapshot content:\n" + actual;
      log.warn(message);
      FileUtil.writeFile(file, actualString);
      tempFile.delete();
      result = Result.INITIAL;
      return this;
    }
  }

  public String toString() { return "Snapshot(" + testClass + "_" + testMethod + "_" + name + ")"; }

  public static Snapshot assertMatchSnapshot(String name, Object actual) {
    return of(name, actual).compareOrRecord();
  }

  /** For testing only */
  public static Snapshot of(String name, Object actual) {
    StackTraceElement si = ClassUtil.findCallerStackTrace(Snapshot.class);
    return new Snapshot(si.getClassName(), si.getMethodName(), name, actual);
  }
}
