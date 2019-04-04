/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.Reader;

/**
 * extend JSONToken with following additions:
 * 1. add method of nextTopValue(), an extension of nextValue(), for unquoted String,
 *  it will not terminate when encountering JSON reserved characters
 *
 */
//TODO: support comments
public class JSONTokenerExt extends JSONTokener {
  public JSONTokenerExt(Reader reader) {
    super(reader);
  }

  @Override
  public Object nextValue() throws JSONException {
    return nextValue(false);
  }

  /**
   * <P>Note: Adopted from JSONTokener.nextValue(), if isTop is true, it will handle unquoted
   * String without terminating when encounter JSON reserved characters.
   *
   * <p>
   * Get the next value. The value can be a Boolean, Double, Integer,
   * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
   * @throws JSONException If syntax error.
   *
   * @param isTop  If it's a top value
   * @return An object.
   */
  public Object nextValue(boolean isTop) throws JSONException {
    char c = this.nextClean();

    switch (c) {
      case '"':
      case '\'':
      case '`':
        return this.nextString(c);
      case '{':
        this.back();
        return new JSONObject(this);
      case '[':
        this.back();
        return new JSONArray(this);
    }

    /*
     * Handle unquoted text. This could be the values true, false, or
     * null, or it can be a number. An implementation (such as this one)
     * is allowed to also accept non-standard forms.
     *
     * Accumulate characters until we reach the end of the text or a
     * formatting character.
     */
    StringBuilder sb = new StringBuilder();
    // while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {  // Original logic, Keep for reference
    while (c >= ' ' && (isTop || ",:]}/\\\"[{;=#".indexOf(c) < 0)) {
      sb.append(c);
      c = this.next();
    }
    this.back();

    String string = sb.toString().trim();
    //    if ("".equals(string)) {  // Original logic, Keep for reference
    //      throw this.syntaxError("Missing value");
    //    }
    return JSONObject.stringToValue(string);
  }

  /**
   * Note: Adopted from JSONTokener.nextString(), it will support "`" quoted multiple Strings
   *
   * Return the characters up to the next close quote character.
   * Backslash processing is done. The formal JSON format does not
   * allow strings in single quotes, but an implementation is allowed to
   * accept them.
   * @param quote The quoting character, either
   *      <code>"</code>&nbsp;<small>(double quote)</small> or
   *      <code>'</code>&nbsp;<small>(single quote)</small>.
   * @return      A String.
   * @throws JSONException Unterminated string.
   */
  public String nextString(char quote) throws JSONException {
    boolean isMultiLine = quote == '`';
    char c;
    StringBuilder sb = new StringBuilder();
    for (;;) {
      c = this.next();
      switch (c) {
        case '\\':
          c = this.next();
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
              try {
                sb.append((char)Integer.parseInt(this.next(4), 16));
              } catch (NumberFormatException e) {
                throw this.syntaxError("Illegal escape.", e);
              }
              break;
            case '"':
            case '\'':
            case '\\':
            case '/':
              sb.append(c);
              break;
            default:
              throw this.syntaxError("Illegal escape.");
          }
          break;
        case 0:
        case '\n':
        case '\r':
          if (!isMultiLine)
            throw this.syntaxError("Unterminated string");
        default:
          if (c == quote) {
            return sb.toString();
          }
          sb.append(c);
      }
    }
  }
}
