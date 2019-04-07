/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.treedoc;

import com.ebay.jsoncodercore.factory.InjectableInstance;
import com.ebay.jsoncodercore.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.IOException;

public class TDJSONWriter {
  @Accessors(chain = true)
  public static class JSONOption {
    @Getter private int indentFactor;
    @Getter @Setter boolean alwaysQuoteName = true;
    @Getter @Setter char quoteChar = '"';
    @Getter private String indentStr = "";  //Used internally
    public JSONOption setIndentFactor(int _indentFactor) {
      this.indentFactor = _indentFactor;
      indentStr = StringUtil.appendRepeatedly(new StringBuilder(), ' ', indentFactor).toString();
      return this;
    }
  }

  public final static InjectableInstance<TDJSONWriter> instance = InjectableInstance.of(TDJSONWriter.class);
  public static TDJSONWriter getInstance() { return instance.get(); }

  public String writeAsString(TDNode node) { return writeAsString(node, new JSONOption()); }
  public String writeAsString(TDNode node, JSONOption opt) {
    StringBuilder out = new StringBuilder();
    write(out, node, opt);
    return out.toString();
  };

  public void write(Appendable out, TDNode node, JSONOption opt) { write(out, node, opt, ""); };

  @SneakyThrows
  public void write(Appendable out, TDNode node, JSONOption opt, String indentStr) {
    if (node == null) {
      out.append("null");
      return;
    }

    boolean isCompact = opt.getIndentFactor() == 0;
    String childIndentStr = "";
    if (!isCompact)
      childIndentStr = indentStr + opt.getIndentStr();

    switch (node.type) {
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
  private void writeMap(Appendable out, TDNode node, JSONOption opt, String indentStr, String childIndentStr) {
    out.append('{');
    if (node.children != null) {
      for (int i = 0; i < node.children.size(); i++){
        TDNode cn = node.children.get(i);
        if (opt.indentFactor > 0) {
          out.append('\n');
          out.append(childIndentStr);
        }
        if (!StringUtil.isJavaIdentifier(cn.key) || opt.alwaysQuoteName)  // Quote the key in case  it's not valid java identifier
          writeQuotedString(out, cn.key, opt.quoteChar);
        else
          out.append(cn.key);
        out.append(":");
        write(out, cn, opt, childIndentStr);
        if (i < node.children.size() - 1) // No need "," for last entry
          out.append(",");
      }

      if (opt.indentFactor > 0 && !node.children.isEmpty()) {
        out.append('\n');
        out.append(indentStr);
      }
    }

    out.append('}');
  }

  @SneakyThrows
  private void writeArray(Appendable out, TDNode node, JSONOption opt, String indentStr, String childIndentStr) {
    out.append('[');
    if (node.children != null) {
      for (int i = 0; i < node.children.size(); i++) {
        TDNode cn = node.children.get(i);
        if (opt.indentFactor > 0) {
          out.append('\n');
          out.append(childIndentStr);
        }
        write(out, cn, opt, childIndentStr);
        if (i < node.children.size() - 1) // No need "," for last entry
          out.append(",");
      }

      if (opt.indentFactor > 0 && !node.children.isEmpty()) {
        out.append('\n');
        out.append(indentStr);
      }
    }

    out.append(']');
  }

  @SneakyThrows
  private void writeSimple(Appendable out, TDNode node, JSONOption opt) {
    if (node.val instanceof String) {
      writeQuotedString(out, (String)node.val, opt.quoteChar);
      return;
    }

    if (node.val instanceof Character) {
      writeQuotedString(out, String.valueOf(node.val), opt.quoteChar);
      return;
    }

    out.append(node.val.toString());
  }

  private void writeQuotedString(Appendable out, String str, char quoteChar) throws IOException {
    out.append(quoteChar);
    out.append(StringUtil.cEscape(str, quoteChar, true));
    out.append(quoteChar);
  }
}
