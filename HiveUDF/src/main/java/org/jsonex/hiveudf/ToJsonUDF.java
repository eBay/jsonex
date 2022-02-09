package org.jsonex.hiveudf;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.jsoncoder.JSONCoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
@Description(name = "to_json",
    value = "to_json(array(obj1, obj2,...)) - Serialize to json. \n",
    extended = "Example:\n"
        + "  > select to_json(*) from tbl")
@Slf4j
public class ToJsonUDF extends GenericUDF {
  public String evaluate(Object obj) throws HiveException {
    return JSONCoder.get().encode(obj);
  }
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
    if (arguments.length == 1) {
      return toJson(toJavaObj(arguments[0].get(), inspectors[0]));
    } else {
      Map<String, Object> map = new HashMap<>();
      for (int i = 0; i < arguments.length; i++)
        map.put(getColumnName(arguments, i),
            toJavaObj(
                arguments[i].get(),
                inspectors[i]));
      return toJson(map);
    }
  }

  public static String getColumnName(DeferredObject[] arguments, int i) {
    try {
      // Use undocumented attributes, may not work for different version
      // Defined in object: ExprNodeGenericFuncEvaluator$DeferredExprObject
      return (String) ClassUtil.getObjectByPath(null, arguments[i], "eval.expr.column");
    } catch (Exception e) {
      return String.valueOf(i);
    }
  }

  private static String toJson(Object obj) {
//    JSONCoderOption opt = JSONCoderOption.of().setShowType(true).setShowPrivateField(true).setDedupWithRef(true)
//        .setShowTransientField(true).addSkippedClasses(HiveConf.class);
    return JSONCoder.get().encode(obj);
  }

  @Override
  public String getDisplayString(String[] children) {
    return getStandardDisplayString("map_values", children);
  }

  public static Object toJavaObj(Object val, ObjectInspector inspector) {
    if (inspector instanceof PrimitiveObjectInspector)
      return ((PrimitiveObjectInspector)inspector).getPrimitiveJavaObject(val);
    if (inspector instanceof MapObjectInspector) {
      Map<Object, Object> result = new HashMap<>();
      MapObjectInspector mapInspector = (MapObjectInspector) inspector;
      for (Map.Entry<?,?> entry : mapInspector.getMap(val).entrySet())
        result.put(toJavaObj(entry.getKey(), mapInspector.getMapKeyObjectInspector()),
            toJavaObj(entry.getValue(), mapInspector.getMapValueObjectInspector()));
      return result;
    } else if (inspector instanceof ListObjectInspector) {
      List<Object> result = new ArrayList<>();
      ListObjectInspector listInspector = (ListObjectInspector) inspector;
      for (Object item : listInspector.getList(val))
        result.add(toJavaObj(item, listInspector.getListElementObjectInspector()));
      return result;
    } else if (inspector instanceof StructObjectInspector) {
      Map<Object, Object> result = new HashMap<>();
      StructObjectInspector structInspector = (StructObjectInspector) inspector;
      List<Object> list = structInspector.getStructFieldsDataAsList(val);
      int i = 0;
      for (StructField field : structInspector.getAllStructFieldRefs())
        result.put(field.getFieldName(), toJavaObj(list.get(i++), field.getFieldObjectInspector()));
      return result;
    }
    return val;
  }
}