package org.jsonex.hiveudf;

import org.junit.Ignore;
import org.junit.Test;

import static org.jsonex.hiveudf.TestUtil.*;
import static org.junit.Assert.assertEquals;

// Reference: https://github.com/apache/hive/blob/master/ql/src/test/org/apache/hadoop/hive/ql/udf/generic/TestGenericUDFSortArray.java
@Ignore("Failed to run on Java17 with error: java.lang.NoClassDefFoundError: Could not initialize class org.apache.hadoop.hive.common.StringInternUtils\n")
public class UDFUtilTest {
  @Test public void testToJavaObj() {
    assertEquals("{key:{age:10,gender:'m'}}", toJson(UDFUtil.toJavaObj(buildMapArgs(), buildMapOI())));
  }
}
