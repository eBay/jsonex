/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

@Slf4j
public abstract class CharSource {
  private final static int MAX_STRING_LEN = 20000;
  private final static String SPACE_CHARS =" \n\r\t";

  final Bookmark bookmark = new Bookmark();

  public abstract char read();
  public abstract char peek(int i);
  public abstract boolean isEof(int i);

  @SneakyThrows
  public Bookmark getBookmark() { return bookmark.clone(); }
  public int getPos() { return bookmark.pos; }
  public boolean isEof() { return isEof(0); }
  public char peek() { return peek(0); }

  /**
   * Skip chars until eof or length or predicate condition matches
   * If target is set, the skipped the chars will be saved in the target
   *
   * @return true The terminate condition matches. otherwise, could be EOF or length matches
   */
  public abstract boolean readUntil(Predicate<CharSource> predicate, StringBuilder target, int minLen, int maxLen);
  public boolean readUntil(Predicate<CharSource> predicate, StringBuilder target) { return readUntil(predicate, target, 0, MAX_STRING_LEN); }
  public boolean skipUntil(Predicate<CharSource> predicate) { return readUntil(predicate, null, 0, Integer.MAX_VALUE); }

  public boolean readUntil(final String terminator, final boolean include, StringBuilder target, int minLen, int maxLen) {
    return readUntil(s -> terminator.indexOf(s.peek(0)) >= 0 == include, target, minLen, maxLen);
  }
  public boolean readUntil(final String terminator, StringBuilder target) { return readUntil(terminator, true, target, 0, MAX_STRING_LEN); }
  public String readUntil(final String terminator) { return readUntil(terminator, 0, Integer.MAX_VALUE); }
  public String readUntil(final String terminator, int minLen, int maxLen) {
    StringBuilder sb = new StringBuilder();
    readUntil(terminator, true, sb, minLen, maxLen);
    return sb.toString();
  }

  public boolean skipUntil(final String terminator, final boolean include) { return readUntil(terminator, include, null, 0, Integer.MAX_VALUE); }
  public boolean skipUntil(final String terminator) { return skipUntil(terminator, true); }
  public boolean skipSpaces() { return skipUntil(SPACE_CHARS, false); }

  public boolean read(StringBuilder target, int len) {
    return readUntil(s -> true, target, len, len);
  }

  public String read(int len) {
    StringBuilder sb = new StringBuilder();
    read(sb, len);
    return sb.toString();
  }

  public boolean skip(int len) { return read(null, len); }


  public boolean readUntilMatch(final String str, final boolean skipStr, StringBuilder target, int minLen, int maxLen) {
    boolean matches = readUntil(s -> startsWidth(str), target, minLen, maxLen);
    if (matches && skipStr)
      skip(str.length());
    return matches;
  }

  public boolean readUntilMatch(final String str, final boolean skipStr, StringBuilder target) {
    return readUntilMatch(str, skipStr, target, 0, MAX_STRING_LEN);
  }

  public boolean skipUntilMatch(final String str, final boolean skipStr) {
    return readUntilMatch(str, skipStr, null, 0, Integer.MAX_VALUE);
  }

  public String peekString(int len) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      if (isEof(i))
        break;
      sb.append(peek(i));
    }
    return sb.toString();
  }

  public boolean startsWidth(String str) {
    if (isEof(str.length()))
      return false;
    for (int i=0; i<str.length(); i++){
      if(peek(i) != str.charAt(i))
        return false;
    }
    return true;
  }

  // For performance, avoid creating String object every time
  private String getTermStrWithQuoteAndEscape(char quote) {
    switch (quote) {
      case '\'': return "\\'";
      case '"': return "\\\"";
      case '`': return "\\`";
      default: return "\\";
    }
  }

  public String readQuotedString(char quote) {
    return readQuotedString(quote, new StringBuilder()).toString();
  }

  public StringBuilder readQuotedString(char quote, StringBuilder sb) {
    String terminator = getTermStrWithQuoteAndEscape(quote);
    int pos = getPos();
    while (true) {
      if (!readUntil(terminator, sb))
        throw new EOFRuntimeException("Can't find matching quote at position:" + pos);
      char c = read();
      if (c == quote) {
        break;
      }
      // c should be '/', tt's a escape sequence
      c = read();
      switch (c) {
        case 'b':
          sb.append('\b');
          break;
        case 't':
          sb.append('\t');
          break;
        case 'n':
          sb.append('\n');
          break;
        case 'f':
          sb.append('\f');
          break;
        case 'r':
          sb.append('\r');
          break;
        case 'u':
          String code = this.read(4);
          try {
            sb.append((char)Integer.parseInt(code, 16));
          } catch (NumberFormatException e) {
            throw createParseRuntimeException("Escaped unicode with invalid number: " + code);
          }
          break;
        case '\n':
        case '\r':
          break;   // Assume it's a line continuation
        case '"':
        case '\'':
        case '\\':
        case '`':
        case '/':
          sb.append(c);
          break;
        default:
          if (isOctDigit(c))
            sb.append((char)readOctNumber(c - '0'));
          else
            throw createParseRuntimeException("Invalid escape sequence:" + c);
      }
    }

    return sb;
  }

  private int readOctNumber(int num) {
    for (int i = 0; i < 2; i++) {
      char d = peek();
      if (!isOctDigit(d))
        break;
      int newNum = num * 8 + (d - '0');
      if (newNum > 255)
        break;
      num = newNum;
      read();
    }
    return num;
  }

  static boolean isOctDigit(char c) { return '0' <= c && c <= '8'; }

//  public String dump() {
//    StringBuilder result = new StringBuilder();
//    result.append("," + bookmark + ": string=" + peek(5));
//    return result.toString();
//  }

  public ParseRuntimeException createParseRuntimeException(String message) {
    return new ParseRuntimeException(message, this.getBookmark(), this.peekString(5));
  }
}
