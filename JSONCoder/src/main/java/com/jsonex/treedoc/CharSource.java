/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc;

import com.jsonex.core.type.Predicate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
  public abstract boolean readUntil(int length, Predicate<CharSource> predicate, StringBuilder target);
  public boolean readUntil(Predicate<CharSource> predicate, StringBuilder target) { return readUntil(MAX_STRING_LEN, predicate, target); }
  public boolean skipUntil(Predicate<CharSource> predicate) { return readUntil(Integer.MAX_VALUE, predicate, null); }

  public boolean readUntil(int length, final String terminator, final boolean include, StringBuilder target) {
    return readUntil(length, new Predicate<CharSource>() {
      @Override public boolean test(CharSource THIS) { return terminator.indexOf(THIS.peek(0)) >= 0 == include; }
    }, target);
  }
  public boolean readUntil(final String terminator, StringBuilder target) { return readUntil(MAX_STRING_LEN, terminator, true, target); }
  public String readUntil(final String terminator) {
    StringBuilder sb = new StringBuilder();
    readUntil(terminator, sb);
    return sb.toString();
  }
  public boolean readUntil(final String terminator, final boolean include, StringBuilder target) { return readUntil(MAX_STRING_LEN, terminator, include, target); }

  public boolean skipUntil(final String terminator, final boolean include) { return readUntil(Integer.MAX_VALUE, terminator, include, null); }
  public boolean skipUntil(final String terminator) { return skipUntil(terminator, true); }
  public boolean skipSpaces() { return skipUntil(SPACE_CHARS, false); }

  public boolean read(int length, StringBuilder target) {
    return readUntil(length, new Predicate<CharSource>() {
      @Override public boolean test(CharSource THIS) { return false; }
    }, target);
  }
  public String read(int length) {
    StringBuilder sb = new StringBuilder();
    read(length, sb);
    return sb.toString();
  }

  public boolean skip(int length) { return read(length, null); }


  public boolean readUntilMatch(int length, final String str, final boolean skipStr, StringBuilder target) {
    boolean matches = readUntil(length, new Predicate<CharSource>() {
      @Override public boolean test(CharSource THIS) { return startsWidth(str); }
    }, target);
    if (matches && skipStr)
      read(str.length(), null);
    return matches;
  }

  public boolean readUntilMatch(final String str, final boolean skipStr, StringBuilder target) {
    return readUntilMatch(MAX_STRING_LEN, str, skipStr, target);
  }

  public boolean skipUntilMatch(final String str, final boolean skipStr) {
    return readUntilMatch(Integer.MAX_VALUE, str, skipStr, null);
  }

  public String peak(int len) {
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
    StringBuilder sb = new StringBuilder();
    String terminator = getTermStrWithQuoteAndEscape(quote);
    int pos = getPos();
    while(true) {
      if(!readUntil(terminator, sb))
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
            throw new ParseRuntimeException("escaped unicode with invalid number: " + code, getBookmark(), peak(5));
          }
          break;
        case '"':
        case '\'':
        case '\\':
        case '`':
        case '/':
          sb.append(c);
          break;
        default:
          throw new ParseRuntimeException("invalid escape sequence:" + c, getBookmark(), peak(5));
      }
    }

    return sb.toString();
  }

  public String dump() {
    StringBuilder result = new StringBuilder();
    result.append("," + bookmark + ": string=" + peak(5));
    return result.toString();
  }
}
