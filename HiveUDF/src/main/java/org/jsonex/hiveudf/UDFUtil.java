package org.jsonex.hiveudf;

import lombok.SneakyThrows;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.jsonex.core.util.ClassUtil;

import java.util.*;

public class UDFUtil {
  @SneakyThrows
  public static Map<String, Object> toJavaMap(GenericUDF.DeferredObject[] arguments, ObjectInspector[] inspectors) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i < arguments.length; i++)
      map.put(getColumnName(arguments, i),
          toJavaObj(
              arguments[i].get(),
              inspectors[i]));
    return map;
  }

  public static String getColumnName(GenericUDF.DeferredObject[] arguments, int i) {
    try {
      // Use undocumented attributes, may not work for different version
      // Defined in object: ExprNodeGenericFuncEvaluator$DeferredExprObject
      return (String) ClassUtil.getObjectByPath(null, arguments[i], "eval.expr.column");
    } catch (Exception e) {
      return String.valueOf(i);
    }
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
