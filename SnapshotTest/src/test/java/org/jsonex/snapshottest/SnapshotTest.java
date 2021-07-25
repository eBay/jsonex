package org.jsonex.snapshottest;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsonex.core.util.FileUtil;
import org.junit.Test;

import java.io.File;

import static org.jsonex.jsoncoder.fieldTransformer.FieldTransformer.exclude;
import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SnapshotTest {
  // Remove current path so that it can avoid flaky test when run in different environment
  static String removeCurrentPth(String message) {
    return message.replace(new File("").getAbsolutePath(), "");
  }

  @Test public void testSnapshot() {
    Snapshot snapshot = Snapshot.of("test", "This is actual value");
    assertMatchesSnapshot("snapshot", snapshot);

    // Test initial
    new File(snapshot.getFile()).delete();
    snapshot.compareOrRecord();
    assertEquals(Snapshot.Result.INITIAL, snapshot.getResult());
    assertMatchesSnapshot("InitialMessage", removeCurrentPth(snapshot.getMessage()));

    // Test matches
    snapshot.compareOrRecord();
    assertEquals(Snapshot.Result.MATCHES, snapshot.getResult());
    assertNull(snapshot.getMessage());

    // Test mismatches
    FileUtil.writeFile(snapshot.getFile(), "Some random junk");
    try {
      snapshot.compareOrRecord();
    } catch (AssertionError e) {
      assertMatchesSnapshot("MismatchError", e.getMessage());
    }
    assertEquals(Snapshot.Result.MISMATCH, snapshot.getResult());
    assertMatchesSnapshot("errorMessage", removeCurrentPth(snapshot.getMessage()));
    assertTrue(new File(snapshot.getFile() + ".tmp").exists());

    // Test approved
    FileUtil.copyFrom(snapshot.getFile(), snapshot.getTempFile());
    snapshot.compareOrRecord();
    assertEquals(Snapshot.Result.MATCHES, snapshot.getResult());
    assertNull(snapshot.getMessage());
    assertFalse(new File(snapshot.getFile() + ".tmp").exists());
  }

  @Data @AllArgsConstructor
  static class TestCls {
    String att1;
    String att2;
  }

  @Test public void testWithCustomOptions() {
    SnapshotOption opt = SnapshotOption.of().mutateJsonCoderOption(o ->
        o.addFilterFor(TestCls.class, exclude("att2")));
    assertMatchesSnapshot("testCls", new TestCls("val1", "val2"), opt);
    assertMatchesSnapshot("testCls", new TestCls("val1", "val2_updated"), opt);
  }

  @Test public void testWithNoName() {
    assertMatchesSnapshot("With No name");
  }

  @Test public void testWithNestedCall() {
    inNestedCall();
  }
  private void inNestedCall() {
    Snapshot snapshot = Snapshot.of(null, "In nested call").compareOrRecord();
    assertEquals("testWithNestedCall", snapshot.getTestMethod());
  }
}
