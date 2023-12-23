package org.jsonex.hiveudf;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.jsonex.core.util.MapBuilder;
import org.jsonex.jsoncoder.JSONCoder;
import org.jsonex.jsoncoder.JSONCoderOption;

import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getStandardMapObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getStandardStructObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.writableIntObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory.writableStringObjectInspector;
import static org.jsonex.core.util.ListUtil.listOf;

public class TestUtil {
  public static Object buildMapArgs() {
    return MapBuilder.mapOf(new Text("key"), listOf(new Text("m"), new IntWritable(10))).build();
  }

  public static ObjectInspector buildMapOI() {
    return getStandardMapObjectInspector(
        writableStringObjectInspector,
        getStandardStructObjectInspector(
            listOf("gender", "age"),
            listOf(writableStringObjectInspector, writableIntObjectInspector))
    );
  }

  public static String toJson(Object obj) {
    JSONCoderOption opt = JSONCoderOption.of().setJsonOption(false, '\'', 0).setStrictOrdering(true);
    return JSONCoder.encode(obj, opt);
  }
}
