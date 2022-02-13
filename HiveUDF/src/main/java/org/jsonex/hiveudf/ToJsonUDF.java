package org.jsonex.hiveudf;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.jsonex.jsoncoder.JSONCoder;


/**
  add jar file:///Users/jianwche/opensource/jsonex/HiveUDF/target/HiveUDF-0.1.22.jar;
  add jar file:///Users/jianwche/opensource/jsonex/JSONCoder/target/JSONCoder-0.1.22.jar;
  add jar file:///Users/jianwche/opensource/jsonex/core/target/core-0.1.22.jar;
  add jar file:///Users/jianwche/opensource/jsonex/treedoc/target/treedoc-0.1.22.jar;
  add jar file:///Users/jianwche/opensource/jsonex/csv/target/csv-0.1.22.jar;
  CREATE TEMPORARY FUNCTION to_json AS 'org.jsonex.hiveudf.ToJsonUDF';
  CREATE TEMPORARY FUNCTION to_csv AS 'org.jsonex.hiveudf.ToCSVUDF';

 create table a(i int, m MAP<STRING, STRUCT<gender:STRING, age:INT>>, s STRUCT<address:STRING, zip:STRING>);
  insert into a values(1, map('ab',named_struct('gender', 'm', 'age', 10), 'cd', named_struct('gender', 'f', 'age', 11)), named_struct('address', 'ca', 'zip', '123'));
  create table a(i int, m ARRAY<STRUCT<gender:STRING, age:INT>>);
  select to_json(*) from a;
 select to_json(s) from a;

 1|[a=[m,10],b=[f,20]]|[ca,123]
 */
@Description(name = "to_json",
    value = "to_json(obj1, obj2,...) - Serialize to json. \n",
    extended = "Example:\n"
        + "  > select to_json(*) from tbl")
@Slf4j
public class ToJsonUDF extends GenericUDF {
  ObjectInspector[] inspectors;

  @Override
  public ObjectInspector initialize(ObjectInspector[] inspectors) throws UDFArgumentException {
    this.inspectors = inspectors;
    return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
  }

  // @Override public String[] getRequiredJars() { return new String[]{"ivy://org.jsonex:JSONCoder:0.1.21?transitive=true"}; }

  @Override
  public Object evaluate(DeferredObject[] arguments) throws HiveException {
//    log.info("args=" + toJson(arguments) + "\ninspects:" + toJson(inspectors) + "\nchildren:" + toJson(children));
//    log.info("args=" + toJson(arguments) + "\ninspects:" + toJson(inspectors));
    return arguments.length == 1
        ? toJson(UDFUtil.toJavaObj(arguments[0].get(), inspectors[0]))
        : toJson(UDFUtil.toJavaMap(arguments, inspectors));
  }

  private static String toJson(Object obj) {
//    JSONCoderOption opt = JSONCoderOption.of().setShowType(true).setShowPrivateField(true).setDedupWithRef(true)
//        .setShowTransientField(true).addSkippedClasses(HiveConf.class);
    return JSONCoder.get().encode(obj);
  }

  @Override
  public String getDisplayString(String[] children) {
    return getStandardDisplayString("ToJsonUDF", children);
  }
}