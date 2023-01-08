/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.treedoc.json;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.StringUtil;
import org.jsonex.treedoc.TDNode;
import lombok.SneakyThrows;
import org.jsonex.treedoc.json.TDJSONOption.TextType;

import java.io.IOException;

import static org.jsonex.treedoc.json.TDJSONOption.TextType.*;

public class TDJSONWriter {

  public final static InjectableInstance<TDJSONWriter> instance = InjectableInstance.of(TDJSONWriter.class);
  public static TDJSONWriter get() { return instance.get(); }

  public String writeAsString(TDNode node) { return writeAsString(node, new TDJSONOption()); }
  public String writeAsString(TDNode node, TDJSONOption opt) {
    return write(new StringBuilder(), node, opt).toString();
  }

  public <T extends Appendable> T write(T out, TDNode node, TDJSONOption opt) { return write(out, node, opt, ""); }

  @SneakyThrows
  public <T extends Appendable> T write(T out, TDNode node, TDJSONOption opt, String indentStr) {
    if (node == null)
      return (T) out.append(opt.deco("null", NON_STRING));

    String childIndentStr = "";
    if (opt.hasIndent())
      childIndentStr = indentStr + opt.getIndentStr();

    switch (node.getType()) {
      case MAP: return writeMap(out, node, opt, indentStr, childIndentStr);
      case ARRAY: return writeArray(out, node, opt, indentStr, childIndentStr);
      default: return writeSimple(out, node, opt);
    }
  }

  @SneakyThrows
  <T extends Appendable> T writeMap(T out, TDNode node, TDJSONOption opt, String indentStr, String childIndentStr) {
    out.append(opt.deco(opt.deliminatorObjectStart.substring(0, 1), OPERATOR));
    for (int i = 0; i < node.getChildrenSize(); i++) {
      TDNode cn = opt.applyFilters(node.getChild(i));
      if (cn == null)
        continue;

      if (opt.hasIndent())
        out.append('\n').append(childIndentStr);

      if (!StringUtil.isJavaIdentifier(cn.getKey()) || opt.alwaysQuoteName)  // Quote the key in case  it's not valid java identifier
        writeQuotedString(out, cn.getKey(), opt, KEY);
      else
        out.append(opt.deco(cn.getKey(), KEY));
      out.append(opt.deco(opt.deliminatorKey, OPERATOR));
      write(out, cn, opt, childIndentStr);
      if (i < node.getChildrenSize() - 1) // No need "," for last entry
        out.append(opt.deco(opt.deliminatorValue, OPERATOR));
    }

    if (opt.hasIndent() && node.hasChildren())
      out.append('\n').append(indentStr);

    return (T) out.append(opt.deco(opt.deliminatorObjectEnd.substring(0, 1), OPERATOR));
  }

  @SneakyThrows
  <T extends Appendable> T writeArray(T out, TDNode node, TDJSONOption opt, String indentStr, String childIndentStr) {
    out.append(opt.deco(opt.deliminatorArrayStart.substring(0, 1), OPERATOR));
    if (node.hasChildren()) {
      for (int i = 0; i < node.getChildrenSize(); i++) {
        TDNode cn = node.getChild(i);
        if (opt.hasIndent())
          out.append('\n').append(childIndentStr);

        write(out, cn, opt, childIndentStr);
        if (i < node.getChildrenSize() - 1) // No need "," for last entry
          out.append(opt.deco(opt.deliminatorValue, OPERATOR));
      }

      if (opt.hasIndent() && node.hasChildren())
        out.append('\n').append(indentStr);
    }

    return (T) out.append(opt.deco(opt.deliminatorArrayEnd.substring(0, 1), OPERATOR));
  }

  @SneakyThrows
  <T extends Appendable> T writeSimple(T out, TDNode node, TDJSONOption opt) {
    Object value = node.getValue();
    if (value instanceof String)
      return writeQuotedString(out, (String)value, opt, STRING);

    if (value instanceof Character)
      return writeQuotedString(out, String.valueOf(value), opt, STRING);

    return (T) out.append(opt.deco(String.valueOf(value), NON_STRING));
  }

  <T extends Appendable> T writeQuotedString(T out, String str, TDJSONOption opt, TextType type) throws IOException {
    return (T) out.append(opt.quoteChar)
        .append(opt.deco(StringUtil.cEscape(str, opt.quoteChar, true), type))
        .append(opt.quoteChar);
  }
}
