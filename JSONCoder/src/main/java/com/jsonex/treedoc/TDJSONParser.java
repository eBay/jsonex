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

import java.io.Reader;

public class TDJSONParser {
  public final static InjectableInstance<TDJSONParser> instance = InjectableInstance.of(TDJSONParser.class);
  public static TDJSONParser getInstance() { return instance.get(); }

  public TDNode parse(Reader reader){ return parse(ReaderCharSource.factory.get(reader)); }
  public TDNode parse(String jsonStr){ return parse(ArrayCharSource.factory.get(jsonStr.toCharArray())); }
  public TDNode parse(CharSource in) { return parse(in, new TDNode(null, null)); }

  public TDNode parse(CharSource in, TDNode node) {
    char c = skipSpaceAndComments(in);
    node.start = in.getPos();
    try {
      switch (c) {
        case '{':
          return parseMap(in, node);
        case '[':
          return parseArray(in, node);
        case '"':
        case '\'':
        case '`':
          in.read();
          return node.setValue(in.readQuotedString(c));
        default:
          String str = in.readUntil(",}]\n\r\t").trim();
          if ("null".equals(str))
            return node.setValue(null);
          if ("true".equals(str))
            return node.setValue(true);
          if ("false".equals(str))
            return node.setValue(false);
          if (c == '-' || (c >= '0' && c <= '9')) {
            return node.setValue(parseNumber(str));
          }
          if (str.isEmpty())
            str = in.read(1);  // At least read one to avoid infinite loop
          return node.setValue(str);
      }
    } finally {
      node.length = in.getPos() - node.start;
    }
  }

  private char skipSpaceAndComments(CharSource in) {
    while (!in.isEof()) {
      in.skipSpaces();
      char c = in.peek();
      if (c!='/' || in.isEof(1))
        return c;
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
          return c;
      }
    }
    throw new EOFRuntimeException("In=" + in.dump());
  }

  private TDNode parseMap(CharSource in, TDNode node) {
    node.type = Type.MAP;
    in.read();
    while (true) {
      char c = skipSpaceAndComments(in);
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
        c = skipSpaceAndComments(in);
        if (c != ':')
          throw new ParseRuntimeException("No ':' after key:" + key, in.getBookmark(), in.peak(5));
        in.read();
      } else {
        key = in.readUntil(":");
        if (in.isEof())
          throw new ParseRuntimeException("No ':' after key:" + key, in.getBookmark(), in.peak(5));
        in.read();
      }

      parse(in, node.createChild(key));
    }
    return node;
  }

  private TDNode parseArray(CharSource in, TDNode node) {
    node.type = Type.ARRAY;
    in.read();
    while (true) {
      char c = skipSpaceAndComments(in);
      if (c == ']') {
        in.read();
        break;
      }

      if (c == ',') {
        in.read();
        c = skipSpaceAndComments(in);
        continue;
      }
      parse(in, node.createChild(null));
    }
    return node;
  }

  private Object parseNumber(String str) {
    if (str.indexOf('.') >= 0) {
      try {
        return Double.parseDouble(str);
      } catch(NumberFormatException e) {
        return str;
      }
    } else {
      try {
        long value = Long.parseLong(str);
        if (value < Integer.MAX_VALUE)   // Can't use Ternary here, as the type will be automatically upper casted to long
          return (int) value;
        return value;
      } catch (NumberFormatException e) {
        return str;
      }
    }
  }
}
