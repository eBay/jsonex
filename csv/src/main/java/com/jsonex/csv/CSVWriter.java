package com.jsonex.csv;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.treedoc.TDNode;
import lombok.SneakyThrows;

public class CSVWriter {
  public final static InjectableInstance<CSVWriter> instance = InjectableInstance.of(CSVWriter.class);
  public static CSVWriter get() { return instance.get(); }

  public String writeAsString(TDNode node) { return writeAsString(node, new CSVOption()); }
  public String writeAsString(TDNode node, CSVOption opt) { return write(new StringBuilder(), node, opt).toString(); }

  @SneakyThrows
  public <T extends Appendable> T write(T out, TDNode node, CSVOption opt) {
    if (node.getChildren() != null) {
      for (TDNode row : node.getChildren()) {
        if (row.getChildren() != null) {
          for (TDNode field : row.getChildren()) {
            writeField(out, field, opt);
            out.append(opt.getFieldSepStr());
          }
          out.append(opt.getRecordSepStr());
        }
      }
    }
    return out;
  }

  @SneakyThrows
  private <T extends Appendable> T writeField(T out, TDNode field, CSVOption opt) {
    String quote = opt.getQuoteCharStr();
    String str = "" + field.getValue();
    if (str.contains(quote) || str.contains(opt.getFieldSepStr()) || str.contains(opt.getRecordSepStr())) {
      if (str.contains(quote))
        str = str.replace(quote, quote + quote);
      return (T)out.append(quote).append(str).append(quote);
    }
    return (T)out.append(str);
  }
}
