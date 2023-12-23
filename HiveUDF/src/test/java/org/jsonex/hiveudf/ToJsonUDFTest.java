package org.jsonex.hiveudf;

import lombok.SneakyThrows;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.io.IntWritable;
import org.junit.Ignore;
import org.junit.Test;

import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.writableIntObjectInspector;
import static org.jsonex.hiveudf.TestUtil.buildMapArgs;
import static org.jsonex.hiveudf.TestUtil.buildMapOI;
import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;

// Reference: https://github.com/apache/hive/blob/master/ql/src/test/org/apache/hadoop/hive/ql/udf/generic/TestGenericUDFSortArray.java
@Ignore("Failed to run on Java17 with error: java.lang.NoClassDefFoundError: Could not initialize class org.apache.hadoop.hive.common.StringInternUtils\n")
public class ToJsonUDFTest {
  ToJsonUDF udf = new ToJsonUDF();

  @SneakyThrows
  @Test public void testEvaluateSingleArg() {
    udf.initialize(new ObjectInspector[]{ buildMapOI() });
    assertMatchesSnapshot(udf.evaluate(new DeferredObject[]{ new GenericUDF.DeferredJavaObject(buildMapArgs()) }));
  }

  @SneakyThrows
  @Test public void testEvaluateMultiArg() {
    udf.initialize(new ObjectInspector[]{ buildMapOI(), writableIntObjectInspector});
    udf.getDisplayString(new String[]{ "struct", "int" });
    assertMatchesSnapshot(
        udf.evaluate(new DeferredObject[]{
            new GenericUDF.DeferredJavaObject(buildMapArgs()),
            new GenericUDF.DeferredJavaObject(new IntWritable(100))
        }));
  }
}
