package com.jsonex.snapshottest;

import com.jsonex.core.util.FileUtil;
import org.junit.Test;

import java.io.File;

import static com.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;
import static org.junit.Assert.*;

public class SnapshotTest {
  @Test public void testSnapshot() {
    Snapshot snapshot = Snapshot.of("test", "This is actual value");
    assertMatchesSnapshot("snapshot", snapshot);

    // Test initial
    new File(snapshot.getFile()).delete();
    snapshot.compareOrRecord();
    assertEquals(Snapshot.Result.INITIAL, snapshot.getResult());
    assertMatchesSnapshot("InitialMessage", snapshot.getMessage());

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
    assertMatchesSnapshot("errorMessage", snapshot.getMessage());
    assertTrue(new File(snapshot.getFile() + ".tmp").exists());

    // Test approved
    FileUtil.copyFrom(snapshot.getFile(), snapshot.getTempFile());
    snapshot.compareOrRecord();
    assertEquals(Snapshot.Result.MATCHES, snapshot.getResult());
    assertNull(snapshot.getMessage());
    assertFalse(new File(snapshot.getFile() + ".tmp").exists());
  }
}
