package org.jsonex.hiveudf;

import org.junit.Test;

import static org.jsonex.hiveudf.TestUtil.*;
import static org.junit.Assert.assertEquals;

// Reference: https://github.com/apache/hive/blob/master/ql/src/test/org/apache/hadoop/hive/ql/udf/generic/TestGenericUDFSortArray.java
public class UDFUtilTest {
  @Test public void testToJavaObj() {
    assertEquals("{key:{gender:'m',age:10}}", toJson(UDFUtil.toJavaObj(buildMapArgs(), buildMapOI())));
  }
}