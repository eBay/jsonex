/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc.json;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.core.util.StringUtil;
import com.jsonex.treedoc.TDNode;
import lombok.SneakyThrows;

import java.io.IOException;

public class TDJSONWriter {

  public final static InjectableInstance<TDJSONWriter> instance = InjectableInstance.of(TDJSONWriter.class);
  public static TDJSONWriter get() { return instance.get(); }

  public String writeAsString(TDNode node) { return writeAsString(node, new TDJSONWriterOption()); }
  public String writeAsString(TDNode node, TDJSONWriterOption opt) {
    StringBuilder out = new StringBuilder();
    write(out, node, opt);
    return out.toString();
  }

  public void write(Appendable out, TDNode node, TDJSONWriterOption opt) { write(out, node, opt, ""); }

  @SneakyThrows
  public void write(Appendable out, TDNode node, TDJSONWriterOption opt, String indentStr) {
    if (node == null) {
      out.append("null");
      return;
    }

    boolean isCompact = opt.getIndentFactor() == 0;
    String childIndentStr = "";
    if (!isCompact)
      childIndentStr = indentStr + opt.getIndentStr();

    switch (node.getType()) {
      case MAP:
        writeMap(out, node, opt, indentStr, childIndentStr);
        return;
      case ARRAY:
        writeArray(out, node, opt, indentStr, childIndentStr);
        return;
      default:
        writeSimple(out, node, opt);
    }
  }

  @SneakyThrows
  private void writeMap(Appendable out, TDNode node, TDJSONWriterOption opt, String indentStr, String childIndentStr) {
    out.append('{');
    if (node.getChildren() != null) {
      for (int i = 0; i < node.getChildrenSize(); i++){
        TDNode cn = node.getChild(i);
        if (opt.indentFactor > 0) {
          out.append('\n');
          out.append(childIndentStr);
        }
        if (!StringUtil.isJavaIdentifier(cn.getKey()) || opt.alwaysQuoteName)  // Quote the key in case  it's not valid java identifier
          writeQuotedString(out, cn.getKey(), opt.quoteChar);
        else
          out.append(cn.getKey());
        out.append(":");
        write(out, cn, opt, childIndentStr);
        if (i < node.getChildrenSize() - 1) // No need "," for last entry
          out.append(",");
      }

      if (opt.indentFactor > 0 && node.hasChildren()) {
        out.append('\n');
        out.append(indentStr);
      }
    }

    out.append('}');
  }

  @SneakyThrows
  private void writeArray(Appendable out, TDNode node, TDJSONWriterOption opt, String indentStr, String childIndentStr) {
    out.append('[');
    if (node.hasChildren()) {
      for (int i = 0; i < node.getChildrenSize(); i++) {
        TDNode cn = node.getChild(i);
        if (opt.indentFactor > 0) {
          out.append('\n');
          out.append(childIndentStr);
        }
        write(out, cn, opt, childIndentStr);
        if (i < node.getChildrenSize() - 1) // No need "," for last entry
          out.append(",");
      }

      if (opt.indentFactor > 0 && node.hasChildren()) {
        out.append('\n');
        out.append(indentStr);
      }
    }

    out.append(']');
  }

  @SneakyThrows
  private void writeSimple(Appendable out, TDNode node, TDJSONWriterOption opt) {
    if (node.getValue() instanceof String) {
      writeQuotedString(out, (String)node.getValue(), opt.quoteChar);
      return;
    }

    if (node.getValue() instanceof Character) {
      writeQuotedString(out, String.valueOf(node.getValue()), opt.quoteChar);
      return;
    }

    out.append(String.valueOf(node.getValue()));
  }

  private void writeQuotedString(Appendable out, String str, char quoteChar) throws IOException {
    out.append(quoteChar);
    out.append(StringUtil.cEscape(str, quoteChar, true));
    out.append(quoteChar);
  }
}
