package org.jsonex.csv;

import lombok.SneakyThrows;
import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.treedoc.TDNode;

import java.util.Collection;
import java.util.List;

import static org.jsonex.core.util.ListUtil.join;
import static org.jsonex.core.util.ListUtil.map;

public class CSVWriter {
  public final static InjectableInstance<CSVWriter> instance = InjectableInstance.of(CSVWriter.class);
  public static CSVWriter get() { return instance.get(); }

  public String writeAsString(TDNode node) { return writeAsString(node, new CSVOption()); }
  public String writeAsString(TDNode node, CSVOption opt) { return write(new StringBuilder(), node, opt).toString(); }

  public <T extends Appendable> T write(T out, TDNode node, CSVOption opt) {
    if (!opt.includeHeader) {
      writeRecords(out, node.childrenValueAsListOfList(), opt);
    } else {
      List<String> keys = node.getChildrenKeys();
      if (node.getType() == TDNode.Type.ARRAY && !keys.isEmpty())
        keys.remove( 0);  // Remove array index key
      append(out, encodeRecord(keys, opt), opt._recordSepStr);
      writeRecords(out, node.childrenValueAsListOfList(keys), opt);
    }
    return out;
  }

  public <T extends Appendable, C extends Collection<Object>> T writeRecords(T out, Collection<C> records, CSVOption opt) {
    records.forEach(r -> append(out, encodeRecord(r, opt), opt._recordSepStr));
    return out;
  }

  @SneakyThrows
  private <T extends Appendable> void append(T out, String... strs) {
    for (String s : strs)
      out.append(s);
  }

  public <T> String encodeRecord(Collection<T> fields, CSVOption opt) {
    return join(map(fields, f -> encodeField(f, opt)), opt.fieldSep);
  }

  private String encodeField(Object field, CSVOption opt) {
    String quote = opt._quoteCharStr;
    String str = "" + field;
    if (needQuote(field, opt)) {
      if (str.contains(quote))
        str = str.replace(quote, quote + quote);
      return quote + str + quote;
    }
    return str;
  }

  private static boolean needQuote(Object field, CSVOption opt) {
    if (!(field instanceof String))
      return false;
    String str = (String)field;
    if (str.isEmpty())
      return false;
    return str.charAt(0) == opt.getQuoteChar()
        || str.contains(opt._fieldSepStr)
        || str.contains(opt._recordSepStr)
        || ClassUtil.toSimpleObject(str) != str;
  }
}
