/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import java.util.Arrays;
import java.util.List;

public class StringUtil {
  private final static String EMPTY = "";

  /**
   * checks a String for null and empty String
   */
  public static boolean isEmpty(String str) { return (str == null) || (str.length() == 0); }

  /**
   * Truncates the rightmost characters of a String to a desired length
   */
  public static String getLeft(String src, int length) { return src == null || length > src.length() ? src : src.substring(0, length); }
  
  /**
   * Truncates the leftmost characters of a String to a desired length
   */
  public static String getRight(String src, int length) { return src == null || src.length() < length ? src : src.substring(src.length() - length); }

  public static boolean isDigitOnly(String chkStr) {
    if (chkStr == null)
      return false;
    for (char c : chkStr.toCharArray()) 
      if (!Character.isDigit(c))
        return false;
    return true;
  }
  
  /**
   * Escape Str using SQL way, assume str is not null.
   */
  public static String SQLEscape(String str) {
    StringBuilder sb = new StringBuilder();
    for (char c : str.toCharArray()) {
      if (c == '\'')
        sb.append('\'');
      sb.append(c);
    }
    return sb.toString();
  }
  
  /**
   * Fill String with the fillChar to a fixed length
   *
   * @param str - The string to be filled
   * @param length - The length of the result string
   * @param fillChar - The character to be filled
   * @param fillLeft - If true, the character will be filled in the left
   * @return  the converted String
   */
  public static String fillString(String str, int length, char fillChar, boolean fillLeft) {
    int len = length - str.length();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++)
      sb.append(fillChar);
    if (fillLeft)
      return sb.append(str).toString();
    else
      return str + sb.toString();
  }

  private final static String C_ESC_CHAR = "'\"`\\\b\f\n\r\t";
  private final static String C_ESC_SYMB = "'\"`\\bfnrt";
  private final static char MIN_PRINTABLE_CHAR = ' ';
  
  public static String cEscape(String str) { return cEscape(str, '"', false); }

  public static String cEscape(String str, char quoteChar) { return cEscape(str, quoteChar, false); }

  /**
   * Escape a String using the C style
   * e.g. for string "It's a example" escape to "It\'s a example");
   * This is used by (Java/Javascript/C/C++)Code generator
   *
   * @param str the string to be escaped
   * @param quoteChar The quote char
   * @param isWchar  Is wide character (Unicode)
   * @return The escaped String
   */
  public static String cEscape(String str, char quoteChar, boolean isWchar) {
    if (str == null)
      return null;

    // First scan to check if it needs escape just to avoid create new String object for better performance.
    int i;
    for (i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c < MIN_PRINTABLE_CHAR || C_ESC_CHAR.indexOf(c) >= 0)
        break;
    }
    if (i == str.length())  // No need escape
      return str;
    
    // Second scan, Do escape
    StringBuilder result = new StringBuilder();
    for (i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      // check if it's a special printable char
      int idx = C_ESC_CHAR.indexOf(c);
      if (idx >= 3 || quoteChar == c) {  // first 3 chars are quote chars
        result.append('\\');
        result.append(C_ESC_SYMB.charAt(idx));
      } else if (c < MIN_PRINTABLE_CHAR) {  // check if it's a un-printable char
        if (!isWchar) {
          result.append("\\0");
          result.append(fillString(Integer.toOctalString((int)c),2,'0',true));
        } else {
          result.append("\\u");
          result.append(fillString(Integer.toHexString((int)c),4,'0',true));
        }
      }else
        result.append(c);
    }
    return result.toString();
  }
  
  public static String lowerFirst(String str) {
    if (Character.isLowerCase(str.charAt(0)))
      return str;
    return Character.toLowerCase(str.charAt(0)) + str.substring(1);
  }
  
  public static String upperFirst(String str){
    if (str == null || str.length() == 0 || Character.isUpperCase(str.charAt(0)))
      return str;
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
  
  public static boolean isJavaIdentifier(String str){
    if (str == null || str.length() < 1 || !Character.isJavaIdentifierStart(str.charAt(0)))
      return false;
   
    for (int i=1; i<str.length(); i++){
      if (!Character.isJavaIdentifierPart(str.charAt(i)))
        return false;
    }
      
    return true;
  }
  
  public static int[] splitAsIntArray(String str, String delimiter) {
    if (str == null)
      return null;
    
    String[] strs = str.split(delimiter);
    int[] result = new int[strs.length];
    for (int i = 0; i < strs.length; i++)
      result[i] = Integer.valueOf(strs[i]);
    return result;
  }

  public static String join(byte[] items, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (byte b : items) {
      if (sb.length() > 0)
        sb.append(delimiter);
      sb.append(b & 0xff);
    }
    return sb.toString();
  }

  public static String join(String[] items, String delimiter) { return join(Arrays.asList(items), delimiter); }
  public static String join(List<String> items, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (String itm : items) {
      if (sb.length() > 0)
        sb.append(delimiter);
      sb.append(itm);
    }
    return sb.toString();
  }


  public static StringBuilder appendRepeatedly(StringBuilder result, String str, int times) {
    for (int i=0; i<times; i++)
      result.append(str);
    return result;
  }  

  public static StringBuilder appendRepeatedly(StringBuilder result, char c, int times) {
    for (int i=0; i<times; i++)
      result.append(c);
    return result;
  }

  public static String toTrimmedStr(Object o, int len) { return StringUtil.getLeft(String.valueOf(o), len); }
}

