/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc;

import com.jsonex.treedoc.TDNode.Type;
import com.jsonex.core.factory.InjectableInstance;

public class TDJSONParser {
  public final static InjectableInstance<TDJSONParser> instance = InjectableInstance.of(TDJSONParser.class);
  public static TDJSONParser get() { return instance.get(); }

  public TDNode parse(TDJSONParserOption opt) { return parse(opt.source, opt, new TDNode()); }

  public TDNode parse(CharSource in, TDJSONParserOption opt, TDNode node) {
    if (!skipSpaceAndComments(in))
      return node;

    char c = in.peek();
    node.start = in.getPos();
    try {
      switch (c) {
        case '{':
          return parseMap(in, opt, node, true);
        case '[':
          return parseArray(in, opt, node, true);
        case '"':
        case '\'':
        case '`':
          in.read();
          StringBuilder sb = new StringBuilder();
          in.readQuotedString(c, sb);
          readContinuousString(in, sb);
          return node.setValue(sb.toString());
        default:
          if (node.isRoot()) {
            switch (opt.defaultRootType) {
              case MAP:
                return parseMap(in, opt, node, false);
              case ARRAY:
                return parseArray(in, opt, node, false);
            }
          }
          String str = in.readUntil(",}]\n\r\t").trim();
          if ("null".equals(str))
            return node.setValue(null);
          if ("true".equals(str))
            return node.setValue(true);
          if ("false".equals(str))
            return node.setValue(false);
          if (str.startsWith("0x") || str.startsWith(("0X")))
            return node.setValue(parseNumber(str.substring(2), true));
          if (c == '-' || (c >= '0' && c <= '9'))
            return node.setValue(parseNumber(str, false));
          if (str.isEmpty())
            str = in.read(1);  // At least read one to avoid infinite loop
          return node.setValue(str);
      }
    } finally {
      node.length = in.getPos() - node.start;
    }
  }

  private void readContinuousString(CharSource in, StringBuilder sb) {
    while(skipSpaceAndComments(in)) {
      char c = in.peek();
      if ("\"`'".indexOf(c) < 0)
        break;
      in.read();
      in.readQuotedString(c, sb);
    }
  }

  /**
   * @return true if there's more text left
   */
  public static boolean skipSpaceAndComments(CharSource in) {
    while (in.skipSpaces()) {
      char c = in.peek();
      if (c == '#') {
        if (in.skipUntil("\n"))
          in.skip(1);
        continue;
      }

      if (c!='/' || in.isEof(1))
        return true;
      char c1 = in.peek(1);
      switch (c1) {
        case '/':   //   line comments
          if (in.skipUntil("\n"))
            in.skip(1);
          break;
        case '*':   //	 block comments
          in.skip(2);
          in.skipUntilMatch("*/", true);
          break;
        default:
          return true;
      }
    }
    return false;
  }

  public TDNode parseMap(CharSource in, TDJSONParserOption opt, TDNode node, boolean withStartBracket) {
    node.type = Type.MAP;
    if (withStartBracket)
      in.read();
    while (true) {
      if (!skipSpaceAndComments(in)) {
        if (withStartBracket)
          throw in.createParseRuntimeException("EOF encountered while expecting matching '}'");
        break;
      }

      char c = in.peek();
      if (c == '}') {
        in.read();
        break;
      }

      if (c == ',') { // Skip ,
        in.read();
        continue;
      }

      String key;
      if (c == '"' || c == '\'' || c == '`') {
        in.read();
        key = in.readQuotedString(c);
        if (!skipSpaceAndComments(in))
          break;
        c = in.peek();
        if (c != ':' && c != '{' && c != '[')
          throw in.createParseRuntimeException("No ':' after key:" + key);
      } else {
        key = in.readUntil(":{[").trim();
        if (in.isEof())
          throw in.createParseRuntimeException("No ':' after key:" + key);
        c = in.peek();
      }
      if (c == ':')
        in.read();

      parse(in, opt, node.createChild(key));
    }
    return node;
  }

  private TDNode parseArray(CharSource in, TDJSONParserOption opt, TDNode node, boolean withStartBracket) {
    node.type = Type.ARRAY;
    if (withStartBracket)
      in.read();
    while (true) {
      if (!skipSpaceAndComments(in)) {
        if (withStartBracket)
          throw in.createParseRuntimeException("EOF encountered while expecting matching ']'");
        break;
      }

      char c = in.peek();
      if (c == ']') {
        in.read();
        break;
      }

      if (c == ',') {
        in.read();
        continue;
      }

      parse(in, opt, node.createChild(null));
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
