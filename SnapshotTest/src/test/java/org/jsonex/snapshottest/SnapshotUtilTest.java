package org.jsonex.snapshottest;

import org.junit.Assert;
import org.junit.Test;

public class SnapshotUtilTest {
  @Test public void testFindCallerTestMethod() {
    Assert.assertEquals("testFindCallerTestMethod", SnapshotUtil.findCallerTestMethod().getMethodName());
  }
}
