/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {
  @Test public void testIsEmpty() {
    assertTrue("empty string is empty", StringUtil.isEmpty(""));
    assertTrue("null is empty", StringUtil.isEmpty(null));
    assertFalse("not empty", StringUtil.isEmpty("abc"));
  }

  @Test public void testGetLeftRight() {
    assertEquals("123", StringUtil.getLeft("1234", 3));
    assertEquals("1234", StringUtil.getLeft("1234", 5));

    assertEquals("234", StringUtil.getRight("1234", 3));
    assertEquals("1234", StringUtil.getRight("1234", 5));

    assertEquals("abc", StringUtil.getLeft("abc=def=ghi", '='));
    assertEquals("ghi", StringUtil.getRight("abc=def=ghi", '='));
  }

  @Test public void testIsDigitOnly() {
    assertTrue("1234 contains only digits", StringUtil.isDigitOnly("1234"));
    assertFalse("1234a doesn't contain only digits", StringUtil.isDigitOnly("1234a"));
    assertFalse("null string is not digits only", StringUtil.isDigitOnly(null));
  }

  @Test public void testSQLEscape() {
    assertEquals("abc''d", StringUtil.SQLEscape("abc'd"));
  }

  @Test public void testFillString() {
    assertEquals("12  ", StringUtil.fillString("12", 4, ' ', false));
    assertEquals("  12", StringUtil.fillString("12", 4, ' ', true));
    assertEquals("12  ", StringUtil.fillSpace("12", 4));
    assertEquals("0012", StringUtil.fillZero("12", 4));
  }

  @Test public void testCEscape() {
    String org1 = "a\"\n\t\u0001";
    String dest = "a\\\"\\n\\t\\u0001";
    assertEquals(dest, StringUtil.cEscape(org1, '"', true));
    assertEquals("a\\\"\\n\\t\\001", StringUtil.cEscape(org1));
    String org2 = "'test'";
    String des2 = "\\'test\\'";
    assertEquals(des2, StringUtil.cEscape(org2, '\'', false));
    assertEquals(org2, StringUtil.cEscape(org2));
    assertEquals(null, StringUtil.cEscape(null));
    assertEquals("abc", StringUtil.cEscape("abc"));
  }

  @Test public void testLowerUpperFirst() {
    assertEquals("abcd", StringUtil.lowerFirst("Abcd"));
    assertEquals("abcd", StringUtil.lowerFirst("abcd"));
    assertEquals("Abcd", StringUtil.upperFirst("abcd"));
    assertEquals("Abcd", StringUtil.upperFirst("Abcd"));
  }

  @Test public void testIsJavaIdentifier() {
    assertTrue("Abcd is a java identifier", StringUtil.isJavaIdentifier("Abcd"));
    assertFalse("1Abcd is not a java identifier", StringUtil.isJavaIdentifier("1Abcd"));
    assertFalse("Ab+cd is not a java identifier", StringUtil.isJavaIdentifier("Ab+cd"));
  }

  @Test public void testSplitAsIntArray() {
    assertArrayEquals(new int[]{1,2,3}, StringUtil.splitAsIntArray("1,2,3", ","));
    assertNull(StringUtil.splitAsIntArray(null, ","));
  }

  @Test public void testJoin() {
    assertEquals("1,2,3", StringUtil.join(new byte[]{1,2,3}, ","));
  }

  @Test public void testAppendRepeatedly() {
    assertEquals("123abab", StringUtil.appendRepeatedly(new StringBuilder("123"), 2, "ab").toString());
    assertEquals("123  ", StringUtil.appendRepeatedly(new StringBuilder("123"), 2, ' ').toString());
  }

  @Test public void testToTrimmedStr() {
    assertEquals("12", StringUtil.toTrimmedStr("1234", 2));
  }
}
