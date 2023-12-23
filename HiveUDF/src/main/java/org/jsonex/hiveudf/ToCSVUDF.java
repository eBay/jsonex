package org.jsonex.hiveudf;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.jsonex.core.util.ListUtil;
import org.jsonex.core.util.StringUtil;
import org.jsonex.csv.CSVOption;
import org.jsonex.csv.CSVWriter;
import org.jsonex.jsoncoder.JSONCoder;
import org.jsonex.jsoncoder.JSONCoderOption;

import java.util.*;

import static org.jsonex.core.util.ListUtil.map;


@Description(name = "to_csv",
    value = "to_csv([opts], col1, col2, ...) - Serialize to CSV, if column value is not simple object, the value will be " +
        "serialized as json. If can provide an optional opts parameter as the first parameters in a JSON format to " +
        "specify field separator or quote.  \n" +
        "Avaliable options: fieldSep:char, quoteChar:char, noHeader:boolean, headers:array<string>",
    extended = "Example:\n"
        + "  > SELECT to_csv(*) FROM someTable \n"
        + "  > SELECT to_csv('{fieldSep:|,quoteChar:\"\\'\"}', *) FROM someTable \n"
        + "  > SELECT to_csv('{noHead:true}', *) FROM someTable \n"
        + "  > SELECT to_csv('{headers:[,,,col3,]}', *) FROM someTable \n"
)
@Slf4j
public class ToCSVUDF extends GenericUDF {
  @Data
  public static class CSVOpt extends CSVOption {
    String[] headers;
    boolean noHeader;
  }

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
    CSVOpt opt = new CSVOpt();
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
    if (recordNum++ == 0 && !opt.noHeader) {
      List<Object> headers = new ArrayList<>(map.keySet());
      if (opt.headers != null) {
        for (int i = 0; i < headers.size(); i++)
          if (i < opt.headers.length && !StringUtil.isEmpty(opt.headers[i]))
            headers.set(i, opt.headers[i]);
      }

      sb.append(CSVWriter.get().encodeRecord(headers, opt)).append("\n");
    }
    List<Object> values = map(map.values(), v -> v instanceof List || v instanceof Map
            ? JSONCoder.get().encode(v, new JSONCoderOption().setStrictOrdering(true)) : v);
    sb.append(CSVWriter.get().encodeRecord(values, opt));
    return sb.toString();
  }

  @Override
  public String getDisplayString(String[] children) {
    return getStandardDisplayString("ToCSVUDF", children);
  }
}