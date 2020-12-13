/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc.json;

import com.jsonex.core.charsource.ArrayCharSource;
import com.jsonex.core.charsource.CharSource;
import com.jsonex.core.charsource.ReaderCharSource;
import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.treedoc.TDNode;
import com.jsonex.treedoc.TreeDoc;
import java.io.Reader;

public class TDJSONParser {
  private final static char EOF = '\uFFFF';
  public final static InjectableInstance<TDJSONParser> instance = InjectableInstance.of(TDJSONParser.class);
  public static TDJSONParser get() { return instance.get(); }

  public TDNode parse(String str) { return parse(str, new TDJSONParserOption()); }
  public TDNode parse(String str, TDJSONParserOption opt) { return parse(new ArrayCharSource(str), opt); }

  public TDNode parse(Reader reader) { return parse(reader, new TDJSONParserOption()); }
  public TDNode parse(Reader reader, TDJSONParserOption opt) { return parse(new ReaderCharSource(reader), opt); }
  public TDNode parse(CharSource src) { return parse(src, new TDJSONParserOption()); }
  public TDNode parse(CharSource src, TDJSONParserOption opt) { return parse(src, opt, new TreeDoc(opt.uri).getRoot()); }

  public TDNode parse(CharSource src, TDJSONParserOption opt, TDNode node) { return parse(src, opt, node, true); }

  public TDNode parse(CharSource src, TDJSONParserOption opt, TDNode node, boolean isRoot) {
    char c = skipSpaceAndComments(src);
    if (c == EOF)
      return node;

    node.setStart(src.getBookmark());
    try {
      if (c == '{')
        return parseMap(src, opt, node, true);

      if (c == '[')
        return parseArray(src, opt, node, true);

      if (isRoot) {
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

      String term = ",\n\r";
      if (node.getParent() != null)  // parent.type can either by ARRAY or MAP.
        term = node.getParent().getType() == TDNode.Type.ARRAY ? ",\n\r]" : ",\n\r}";

      String str = src.readUntil(term, 0, Integer.MAX_VALUE).trim();
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

  void readContinuousString(CharSource src, StringBuilder sb) {
    char c;
    while((c = skipSpaceAndComments(src)) != EOF) {
      if ("\"`'".indexOf(c) < 0)
        break;
      src.read();
      src.readQuotedString(c, sb);
    }
  }

  /**
   * @return char next char to read (peeked), if '\uFFFF' indicate it's EOF
   */
  static char skipSpaceAndComments(CharSource src) {
    while (src.skipSpacesAndReturns()) {
      char c = src.peek();
      if (c == '#') {
        if (src.skipUntil("\n"))
          src.skip(1);
        continue;
      }

      if (c != '/' || src.isEof(1))
        return c;
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
          return c1;
      }
    }
    return EOF;
  }

  TDNode parseMap(CharSource src, TDJSONParserOption opt, TDNode node, boolean withStartBracket) {
    node.setType(TDNode.Type.MAP);
    if (withStartBracket)
      src.read();
    for (int i = 0;;) {
      char c = skipSpaceAndComments(src);
      if (c == EOF) {
        if (withStartBracket)
          throw src.createParseRuntimeException("EOF while expecting matching '}' with '{' at " + node.getStart());
        break;
      }

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
        c = skipSpaceAndComments(src);
        if (c == EOF)
          break;
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
        TDNode childNode = parse(src, opt, node.createChild(key), false);
        if (opt.KEY_ID.equals(key) && childNode.getType() == TDNode.Type.SIMPLE)
          node.getDoc().getIdMap().put(childNode.getValue().toString(), node);
      }
      i++;
    }
    return node;
  }

  TDNode parseArray(CharSource src, TDJSONParserOption opt, TDNode node, boolean withStartBracket) {
    node.setType(TDNode.Type.ARRAY);
    if (withStartBracket)
      src.read();
    while (true) {
      char c = skipSpaceAndComments(src);
      if (c == EOF) {
        if (withStartBracket)
          throw src.createParseRuntimeException("EOF encountered while expecting matching ']'");
        break;
      }

      if (c == ']') {
        src.read();
        break;
      }

      parse(src, opt, node.createChild(null), false);
      c = skipSpaceAndComments(src);
      if (c == ',') {
        src.read();
      }
    }
    return node;
  }

  Object parseNumber(String str, boolean isHex) {
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
