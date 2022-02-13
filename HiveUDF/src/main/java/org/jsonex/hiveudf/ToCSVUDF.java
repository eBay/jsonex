package org.jsonex.hiveudf;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.jsonex.core.util.ListUtil;
import org.jsonex.csv.CSVOption;
import org.jsonex.csv.CSVWriter;
import org.jsonex.jsoncoder.JSONCoder;
import org.jsonex.jsoncoder.JSONCoderOption;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jsonex.core.util.ListUtil.map;


/**
  add jar file:///Users/jianwche/opensource/jsonex/HiveUDF/target/HiveUDF-0.1.21.jar;
  add jar file:///Users/jianwche/opensource/jsonex/JSONCoder/target/JSONCoder-0.1.21.jar;
  add jar file:///Users/jianwche/opensource/jsonex/core/target/core-0.1.21.jar;
  add jar file:///Users/jianwche/opensource/jsonex/treedoc/target/treedoc-0.1.21.jar;
  CREATE TEMPORARY FUNCTION to_json AS 'org.jsonex.hiveudf.ToJsonUDF';
  create table a(i int, m MAP<STRING, STRUCT<gender:STRING, age:INT>>, s STRUCT<address:STRING, zip:STRING>);
  insert into a values(1, map('ab',named_struct('gender', 'm', 'age', 10), 'cd', named_struct('gender', 'f', 'age', 11)), named_struct('address', 'ca', 'zip', '123'));
  create table a(i int, m ARRAY<STRUCT<gender:STRING, age:INT>>);
  select to_json(*) from a;
  select to_json(s) from a;

 1|[a=[m,10],b=[f,20]]|[ca,123]
 */
@Description(name = "to_csv",
    value = "to_csv([opts], col1, col2, ...) - Serialize to CSV, if column value is not simple object, the value will be " +
        "serialized as json. If can provide an optional opts parameter as the first parameters in a JSON format to " +
        "specify field separator or quote. \n",
    extended = "Example:\n"
        + "  > select to_csv(*) from tbl"
        + "  > select to_csv('{fieldSep:|,quoteChar:\"\\'\"}', *) from a; from tbl"
)
@Slf4j
public class ToCSVUDF extends GenericUDF {
  ObjectInspector[] inspectors;
  int recordNum;

  @Override
  public ObjectInspector initialize(ObjectInspector[] inspectors) throws UDFArgumentException {
    this.inspectors = inspectors;
    recordNum = 0;
    return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
  }

  // @Override public String[] getRequiredJars() { return new String[]{"ivy://org.jsonex:JSONCoder:0.1.21?transitive=true"}; }

  @Override
  public Object evaluate(DeferredObject[] arguments) throws HiveException {
    StringBuilder sb = new StringBuilder();
    CSVOption opt = new CSVOption();
    Map<String, Object> map = UDFUtil.toJavaMap(arguments, inspectors);
    Optional<String> firstKeyOpt = ListUtil.first(map.keySet());
    if (!firstKeyOpt.isPresent())
      return "";
    String firstKey = firstKeyOpt.get();
    Object firstVal = map.get(firstKey);
    if (firstKey.equals("0") && firstVal instanceof String && ((String) firstVal).startsWith("{")) {  // It's csv opt parameter.
      JSONCoder.get().decodeTo((String) firstVal, opt);
      map.remove(firstKey);
    }
    if (recordNum++ == 0) {
      sb.append(CSVWriter.get().encodeRecord(map.keySet(), opt)).append("\n");
    }
    List<Object> values = map(map.values(), v -> v instanceof List || v instanceof Map
            ? JSONCoder.get().encode(v, new JSONCoderOption().setShowType(true)) : v);
    sb.append(CSVWriter.get().encodeRecord(values, opt));
    return sb.toString();
  }

  @Override
  public String getDisplayString(String[] children) {
    return getStandardDisplayString("ToCSVUDF", children);
  }
}