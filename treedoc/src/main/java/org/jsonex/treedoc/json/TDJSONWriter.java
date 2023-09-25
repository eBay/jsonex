/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.treedoc.json;

import lombok.SneakyThrows;
import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.StringUtil;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.json.TDJSONOption.TextType;
import static org.jsonex.treedoc.json.TDJSONOption.TextType.KEY;
import static org.jsonex.treedoc.json.TDJSONOption.TextType.NON_STRING;
import static org.jsonex.treedoc.json.TDJSONOption.TextType.OPERATOR;
import static org.jsonex.treedoc.json.TDJSONOption.TextType.STRING;
import static org.jsonex.treedoc.json.TDJSONOption.TextType.TYPE;

import java.io.IOException;

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
    if (opt.useTypeWrapper) {
      String type = (String)node.getChildValue(opt.KEY_TYPE);
      if (type != null) out.append(opt.deco(type, TYPE));
    }
    out.append(opt.deco(opt.deliminatorObjectStart.substring(0, 1), OPERATOR));
    for (int i = 0; i < node.getChildrenSize(); i++) {
      TDNode cn = opt.applyFilters(node.getChild(i));
      if (cn == null || (opt.useTypeWrapper && cn.getKey().equals(opt.KEY_TYPE)))
        continue;

      if (opt.hasIndent())
        out.append('\n').append(childIndentStr);

      // Quote the key in case it's not valid java identifier so that it can be parsed back in Javascript
      writeQuotedString(out, cn.getKey(), opt, KEY, !StringUtil.isJavaIdentifier(cn.getKey()) || opt.alwaysQuoteKey);

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
    if (value instanceof Character)
      value = String.valueOf(value);
    return value instanceof String
        ? writeQuotedString(out, (String)value, opt, STRING, opt.alwaysQuoteValue)
        : (T) out.append(opt.deco(String.valueOf(value), NON_STRING));
  }

  <T extends Appendable> T writeQuotedString(T out, String str, TDJSONOption opt, TextType type, boolean alwaysQuote) throws IOException {
    char quoteChar = determineQuoteChar(str, opt, alwaysQuote);
    Appendable result = quoteChar == 0 ? out.append(opt.deco(str, type)) : out.append(quoteChar)
        .append(opt.deco(StringUtil.cEscape(str, quoteChar, true), type))
        .append(quoteChar);
    return (T) result;
  }

  /** return 0 indicate quote is not necessary */
  static char determineQuoteChar(String str, TDJSONOption opt, boolean alwaysQuote) {
    boolean needQuote = alwaysQuote || StringUtil.indexOfAnyChar(str, opt._quoteNeededChars) >= 0;
    if (!needQuote)
      return 0;
    if (opt.quoteChars.length() == 1)
      return opt.quoteChars.charAt(0);

    // Determine which quote char to use
    int counts[] = new int[opt.quoteChars.length()];
    for(char ch : str.toCharArray()) {
      int idx = opt.quoteChars.indexOf(ch);
      if (idx >= 0)
        counts[idx]++;
    }
    int minIdx = 0;  // default to first quote char
    for (int i = 1; i < counts.length; i++)
      if (counts[i] < counts[minIdx])
        minIdx = i;
    return opt.quoteChars.charAt(minIdx);
  }
}
