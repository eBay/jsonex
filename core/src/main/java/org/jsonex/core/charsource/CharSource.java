/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.charsource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.type.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

@Slf4j
public abstract class CharSource {
  private final static int MAX_STRING_LEN = 20000;
  // HTML &nbsp; will be converted to \u00a0, that's why it need to be supported here
  private final static String SPACE_RETURN_CHARS = " \n\r\t\u00a0";
  private final static String SPACE_RETURN_COMMA_CHARS = SPACE_RETURN_CHARS + ",";

  public final Bookmark bookmark = new Bookmark();

  public abstract char read();
  public abstract char peek(int i);
  public abstract boolean isEof(int i);

  @SneakyThrows
  public Bookmark getBookmark() { return bookmark.clone(); }
  public int getPos() { return bookmark.getPos(); }
  public boolean isEof() { return isEof(0); }
  public char peek() { return peek(0); }

  /**
   * Skip chars until eof or length or predicate condition matches
   * If target is set, the skipped the chars will be saved in the target
   *
   * @return true The terminate condition matches. otherwise, could be EOF or length matches
   */
  public abstract boolean readUntil(StringBuilder target, Predicate<CharSource> predicate, int minLen, int maxLen);
  public boolean readUntil(Predicate<CharSource> predicate, StringBuilder target) {
    return readUntil(target, predicate, 0, MAX_STRING_LEN);
  }
  public boolean skipUntil(Predicate<CharSource> predicate) {
    return readUntil(null, predicate, 0, Integer.MAX_VALUE);
  }
  /** @return true Terminal conditions matches  */
  public boolean readUntil(StringBuilder target, String chars, @Nullable Collection<String> strs, boolean include, int minLen, int maxLen) {
    return readUntil(target, s -> (chars.indexOf(s.peek(0)) >= 0 || startsWithAny(strs)) == include, minLen, maxLen);
  }
  public boolean readUntil(StringBuilder target, String terminator) { return readUntil(target, terminator, null); }
    /** @return true Terminal conditions matches  */
  public boolean readUntil(StringBuilder target, String terminator, Collection<String> strs) {
    return readUntil(target, terminator, strs, true, 0, MAX_STRING_LEN);
  }
  public String readUntil(String terminator) { return readUntil(terminator, null,0, Integer.MAX_VALUE); }
  /** @return true Terminal conditions matches  */
  public String readUntil(String terminator, Collection<String> strs) { return readUntil(terminator, strs,0, Integer.MAX_VALUE); }
  /** @return true Terminal conditions matches  */
  public String readUntil(String terminator, Collection<String> strs, int minLen, int maxLen) {
    StringBuilder sb = new StringBuilder();
    readUntil(sb, terminator, strs, true, minLen, maxLen);
    return sb.toString();
  }

  /** @return true Indicates more character in the stream  */
  public boolean skipUntil(String chars, boolean include) {
    return readUntil(null, chars, null, include, 0, Integer.MAX_VALUE);
  }
  /** @return true Indicates more character in the stream  */
  public boolean skipUntil(String terminator) { return skipUntil(terminator, true); }
  /** @return true Indicates more character in the stream  */
  public boolean skipSpacesAndReturns() { return skipUntil(SPACE_RETURN_CHARS, false); }
  public boolean skipSpacesAndReturnsAndCommas() { return skipUntil(SPACE_RETURN_COMMA_CHARS, false); }
  /** @return true Indicates more character in the stream  */
  public boolean skipChars(String chars) { return skipUntil(chars, false); }

  public boolean read(StringBuilder target, int len) { return readUntil(target, s -> true, len, len); }

  public String read(int len) {
    StringBuilder sb = new StringBuilder();
    read(sb, len);
    return sb.toString();
  }

  public boolean skip() { return read(null, 1); }
  public boolean skip(int len) { return read(null, len); }

  public boolean readUntilMatch(StringBuilder target, String str, boolean skipStr, int minLen, int maxLen) {
    boolean matches = readUntil(target, s -> startsWith(str), minLen, maxLen);
    if (matches && skipStr)
      skip(str.length());
    return matches;
  }

  public boolean readUntilMatch(String str, boolean skipStr, StringBuilder target) {
    return readUntilMatch(target, str, skipStr, 0, MAX_STRING_LEN);
  }

  public boolean skipUntilMatch(String str, boolean skipStr) {
    return readUntilMatch(null, str, skipStr, 0, Integer.MAX_VALUE);
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

  public boolean startsWithAny(@Nullable Collection<String> strs) {
    if (strs != null) {
      for (String s : strs)
        if (startsWith(s))
          return true;
    }
    return false;
  }

  public boolean startsWith(String str) {
    if (isEof(str.length()-1))
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
    return readQuotedString( new StringBuilder(), quote).toString();
  }

  public StringBuilder readQuotedString(StringBuilder sb, char quote) {
    String terminator = getTermStrWithQuoteAndEscape(quote);
    // Not calling getBookmark() to avoid clone an object
    int pos = getPos();
    int line = bookmark.getLine();
    int col = bookmark.getCol();
    while (true) {
      if (!readUntil(sb, terminator))
        throw new EOFRuntimeException("Can't find matching quote at position:" + pos + ";line:" + line + ";col:" + col);
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
        case 'v':
          sb.append('\u000B');
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
        default:
          if (isOctDigit(c))
            sb.append((char)readOctNumber(c - '0'));
          else
            sb.append(c);
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
    return new ParseRuntimeException(message, this.getBookmark(), this.peekString(10));
  }
}
