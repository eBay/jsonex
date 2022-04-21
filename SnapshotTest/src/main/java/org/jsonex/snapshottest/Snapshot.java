package org.jsonex.snapshottest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.type.Lazy;
import org.jsonex.core.util.FileUtil;
import org.jsonex.core.util.LangUtil;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

import static org.jsonex.core.util.LangUtil.throwIf;
import static org.jsonex.core.util.StringUtil.isEmpty;

import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Snapshot is a simple test utility to record actual output into snapshot file when the test is first time run,
 * and for subsequent runs, it will verify the new output against the recorded snapshot value. If there's difference,
 * a tmp snapshot file will be generated with the new output in the same directory. You can manually compare the new
 * output and old put to verify if the changes is expected, if it's expected, you can approve the expected changes by
 * just renaming the newly generated tmp file to the original snapshot file. Or just delete the original snapshot file,
 * it will be automatically regenerated with the new version.
 * By default, the snapshot files and the tmp files are stored in following folder structure:
 * src/test/resources/<packageName>/__snapshot__/<testClassName>_<testMethodName>_<snapshotName>.[json|txt].[tmp]
 */
@Data @RequiredArgsConstructor @Slf4j
public class Snapshot {
  enum Result { MATCHES, MISMATCH, INITIAL }

  private SnapshotOption option = new SnapshotOption();
  private final String testClass;
  private final String testMethod;
  private final String name;
  private final Object actual;
  private final Lazy<String> actualString = new Lazy<>();
  private String expected;

  private Result result;  // For testing only
  private String message;  // For testing only

  private String getNameSection() { return  isEmpty(name) ? "" : "_" + name; }

  public String getFile() {
    int packagePos = testClass.lastIndexOf(".");
    throwIf(packagePos < 0, () -> new IllegalArgumentException("Class has not package:" + testClass));
    String baseName = testClass.substring(0, packagePos) + ".__snapshot__" +  testClass.substring(packagePos);
    String ext = actual instanceof String ? ".txt" : option.getSerializer().getFileExtension(actual);
    return option.getTestResourceRoot() + "/" +
        baseName.replace(".", "/") + "_" + testMethod + getNameSection() + ext;
  }

  public String getTempFile() { return getFile() + ".tmp"; }

  public String getActualString() {
    return actualString.getOrCompute(() ->
        LangUtil.getIfInstanceOf(actual, String.class, Function.identity(), this::serialize));
  }

  private String serialize(Object obj) { return option.getSerializer().serialize(obj); }

  /** @return true if snapshot exists and matches*/
  public Snapshot compareOrRecord() {
    File file = new File(getFile());
    String actualString = getActualString();
    File tempFile = new File(getTempFile());
    if (file.exists()) {
      String expected = FileUtil.readFile(file);
      try {
        String formattedActualString = actualString.replace("`", "\"");
        String formattedExpectedString = expected.replace("`", "\"");
        formattedActualString = formattedActualString.replace("\'\"", "`");
        formattedExpectedString = formattedExpectedString.replace("\'\"", "`");
        JSONAssert.assertEquals(formattedActualString, formattedExpectedString, false);
        tempFile.delete();
        result = Result.MATCHES;
        message = null;
        return this; 
      } catch (Exception e) {
        message = this + " and actual mismatch. The actual is record in file:\nfile://" + tempFile.getAbsolutePath() +
            "\nPlease review the difference. If the change is expected, please override the snapshot file with the tmp file.";
        log.error(message);
        FileUtil.writeFile(tempFile, actualString);
        result = Result.MISMATCH;
        throw new AssertionError(this + " and actual mismatch " + "expected:<" + expected + "> but was:<" + getActualString() + ">");
      }
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

  public String toString() { return "Snapshot(" + testClass + "_" + testMethod + getNameSection() + ")"; }

  public static Snapshot assertMatchesSnapshot(Object actual) { return assertMatchesSnapshot(null, actual); }
  public static Snapshot assertMatchesSnapshot(String name, Object actual) { return of(name, actual).compareOrRecord(); }

  public static Snapshot assertMatchesSnapshot(String name, Object actual, SnapshotOption option) {
    return of(name, actual).setOption(option).compareOrRecord();
  }

  public static Snapshot assertMatchesSnapshot(Object actual, SnapshotOption option) {
    return of(null, actual).setOption(option).compareOrRecord();
  }

  /** For testing only */
  public static Snapshot of(String name, Object actual) {
    StackTraceElement si = SnapshotUtil.findCallerTestMethod();
    return new Snapshot(si.getClassName(), si.getMethodName(), name, actual);
  }
}
