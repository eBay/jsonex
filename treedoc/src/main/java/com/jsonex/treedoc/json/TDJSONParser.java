/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc.json;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.treedoc.TDNode;
import com.jsonex.treedoc.TreeDoc;

public class TDJSONParser {
  public final static InjectableInstance<TDJSONParser> instance = InjectableInstance.of(TDJSONParser.class);
  public static TDJSONParser get() { return instance.get(); }

  public TDNode parse(TDJSONParserOption opt) { return parse(opt.source, opt, new TreeDoc(opt.uri).getRoot()); }

  private TDNode parse(CharSource src, TDJSONParserOption opt, TDNode node) {
    if (!skipSpaceAndComments(src))
      return node;

    char c = src.peek();
    node.setStart(src.getBookmark());
    try {
      if (c == '{')
        return parseMap(src, opt, node, true);

      if (c == '[')
        return parseArray(src, opt, node, true);

      if (node.isRoot()) {
        switch (opt.defaultRootType) {
          case MAP:
            return parseMap(src, opt, node, false);
          case ARRAY:
            return parseArray(src, opt, node, false);
          default:;
        }
      }

      if(c == '"' || c == '\'' || c == '`') {
        src.read();
        StringBuilder sb = new StringBuilder();
        src.readQuotedString(c, sb);
        readContinuousString(src, sb);
        return node.setValue(sb.toString());
      }

      String str = src.readUntil(",}]\n\r", 0, Integer.MAX_VALUE).trim();
      if ("null".equals(str))
        return node.setValue(null);
      if ("true".equals(str))
        return node.setValue(true);
      if ("false".equals(str))
        return node.setValue(false);
      if (str.startsWith("0x") || str.startsWith(("0X")))
        return node.setValue(parseNumber(str.substring(2), true));
      if (c == '-' || c == '+' || c == '.' || (c >= '0' && c <= '9'))
        return node.setValue(parseNumber(str, false));
      return node.setValue(str);
    } finally {
      node.setEnd(src.getBookmark());
    }
  }

  private void readContinuousString(CharSource src, StringBuilder sb) {
    while(skipSpaceAndComments(src)) {
      char c = src.peek();
      if ("\"`'".indexOf(c) < 0)
        break;
      src.read();
      src.readQuotedString(c, sb);
    }
  }

  /**
   * @return true if there's more text left
   */
  public static boolean skipSpaceAndComments(CharSource src) {
    while (src.skipSpaces()) {
      char c = src.peek();
      if (c == '#') {
        if (src.skipUntil("\n"))
          src.skip(1);
        continue;
      }

      if (c != '/' || src.isEof(1))
        return true;
      char c1 = src.peek(1);
      switch (c1) {
        case '/':   // line comments
          if (src.skipUntil("\n"))
            src.skip(1);
          break;
        case '*':   // block comments
          src.skip(2);
          src.skipUntilMatch("*/", true);
          break;
        default:
          return true;
      }
    }
    return false;
  }

  public TDNode parseMap(CharSource src, TDJSONParserOption opt, TDNode node, boolean withStartBracket) {
    node.setType(TDNode.Type.MAP);
    if (withStartBracket)
      src.read();
    for (int i = 0;;) {

      if (!skipSpaceAndComments(src)) {
        if (withStartBracket)
          throw src.createParseRuntimeException("EOF while expecting matching '}' with '{' at " + node.getStart());
        break;
      }

      char c = src.peek();
      if (c == '}') {
        src.read();
        break;
      }

      if (c == ',') { // Skip ,
        src.read();
        continue;
      }

      String key;
      if (c == '"' || c == '\'' || c == '`') {
        src.read();
        key = src.readQuotedString(c);
        if (!skipSpaceAndComments(src))
          break;
        c = src.peek();
        if (c != ':' && c != '{' && c != '[' && c != ',' && c != '}')
          throw src.createParseRuntimeException("No ':' after key:" + key);
      } else {
        key = src.readUntil(":{[,}", 1, Integer.MAX_VALUE).trim();
        if (src.isEof())
          throw src.createParseRuntimeException("No ':' after key:" + key);
        c = src.peek();
      }
      if (c == ':')
        src.read();

      if (c == ',' || c == '}')  // If there's no ':', we consider it as indexed value (array)
        node.createChild(i + "").setValue(key);
      else {
        TDNode childNode = parse(src, opt, node.createChild(key));
        if (opt.KEY_ID.equals(key) && childNode.getType() == TDNode.Type.SIMPLE)
          node.getDoc().getIdMap().put(childNode.getValue().toString(), node);
      }
      i++;
    }
    return node;
  }

  private TDNode parseArray(CharSource src, TDJSONParserOption opt, TDNode node, boolean withStartBracket) {
    node.setType(TDNode.Type.ARRAY);
    if (withStartBracket)
      src.read();
    while (true) {
      if (!skipSpaceAndComments(src)) {
        if (withStartBracket)
          throw src.createParseRuntimeException("EOF encountered while expecting matching ']'");
        break;
      }

      char c = src.peek();
      if (c == ']') {
        src.read();
        break;
      }

      if (c == ',') {
        src.read();
        continue;
      }

      parse(src, opt, node.createChild(null));
    }
    return node;
  }

  private Object parseNumber(String str, boolean isHex) {
    if (!isHex && str.indexOf('.') >= 0) {
      try {
        return Double.parseDouble(str);
      } catch(NumberFormatException e) {
        return str;
      }
    }

    try {
      long value = Long.parseLong(str, isHex ? 16 : 10);
      if (value < Integer.MAX_VALUE)   // Can't use Ternary here, as the type will be automatically upper casted to long
        return (int) value;
      return value;
    } catch (NumberFormatException e) {
      return str;
    }
  }
}
