package org.jsonex.hiveudf;

import lombok.SneakyThrows;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.jsonex.core.util.MapBuilder;
import org.jsonex.jsoncoder.JSONCoder;
import org.jsonex.jsoncoder.JSONCoderOption;
import org.junit.Test;

import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getStandardMapObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getStandardStructObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.writableIntObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.writableStringObjectInspector;
import static org.jsonex.core.util.ListUtil.listOf;
import static org.junit.Assert.assertEquals;

// Reference: https://github.com/apache/hive/blob/master/ql/src/test/org/apache/hadoop/hive/ql/udf/generic/TestGenericUDFSortArray.java
public class ToJsonUDFTest {
  private static Object buildMapArgs() {
    return MapBuilder.mapOf(new Text("key"), listOf(new Text("m"), new IntWritable(10))).build();
  }

  private static ObjectInspector buildMapOI() {
    return getStandardMapObjectInspector(
        writableStringObjectInspector,
        getStandardStructObjectInspector(
            listOf("gender", "age"),
            listOf(writableStringObjectInspector, writableIntObjectInspector))
    );
  }

  @Test public void testToJavaObj() {
    assertEquals("{key:{gender:'m',age:10}}", toJson(ToJsonUDF.toJavaObj(buildMapArgs(), buildMapOI())));
  }

  @SneakyThrows
  @Test public void testEvaluateSingleArg() {
    ToJsonUDF udf = new ToJsonUDF();
    udf.initialize(new ObjectInspector[]{ buildMapOI() });
    assertEquals("{\"key\":{\"gender\":\"m\",\"age\":10}}", udf.evaluate(new DeferredObject[]{ new GenericUDF.DeferredJavaObject(buildMapArgs()) }));
  }

  @SneakyThrows
  @Test public void testEvaluateMultiArg() {
    ToJsonUDF udf = new ToJsonUDF();
    udf.initialize(new ObjectInspector[]{ buildMapOI(), writableIntObjectInspector});
    udf.getDisplayString(new String[]{ "struct", "int" });
    assertEquals("{\"0\":{\"key\":{\"gender\":\"m\",\"age\":10}},\"1\":100}",
        udf.evaluate(new DeferredObject[]{
            new GenericUDF.DeferredJavaObject(buildMapArgs()),
            new GenericUDF.DeferredJavaObject(new IntWritable(100))
        }));
  }


  private static String toJson(Object obj) {
    JSONCoderOption opt = JSONCoderOption.of().setJsonOption(false, '\'', 0);
    return JSONCoder.encode(obj, opt);
  }

}
