package com.jsonex.core.charsource;

import org.junit.Test;

import static junit.framework.Assert.*;

public abstract class BaseCharSourceTest {
  protected abstract CharSource createCharSource(String str, int startIndex, int endIndex);
  protected CharSource createCharSource(String str) { return createCharSource(str, 0, str.length()); }

  @Test public void testCharArraySource() {
    CharSource cs = createCharSource("--0123\n--", 2, 7);
    assertEquals('0', cs.read());
    assertEquals('1', cs.read());
    assertEquals('2', cs.peek(0));
    assertEquals('3', cs.peek(1));
    assertEquals('2', cs.read());
    assertEquals('3', cs.read());
    assertEquals("Bookmark(line=0, col=4, pos=4)", cs.getBookmark().toString());
    assertEquals('\n', cs.read());
    assertEquals("Bookmark(line=1, col=0, pos=5)", cs.getBookmark().toString());
    assertTrue("should eof", cs.isEof(0));
    try {
      cs.read();
      assertFalse("Should throw EOF", false);
    } catch (EOFRuntimeException e) {
    }
  }

  @Test public void testParseText() {
    CharSource cs = createCharSource(" \u00a0Text before /* some comments */ Text after");
    cs.skipSpacesAndReturns();
    assertEquals(2, cs.getPos());

    StringBuilder target = new StringBuilder();
    assertTrue("should match /*", cs.readUntilMatch("/*", false, target, 0, 1000));
    assertEquals("Text before ", target.toString());
    assertTrue("should start with /*", cs.startsWidth("/*"));
    cs.skip(2);  // skip /*

    target = new StringBuilder();
    assertTrue("should match with */", cs.readUntilMatch("*/", false, target));
    assertEquals(" some comments ", target.toString());

    target = new StringBuilder();
    assertTrue("should match with */", cs.readUntilMatch("*/", false, target));
  }

  @Test public void testReadQuotedString() {
    assertReadQuotedString(
        "'It\\'s a quoted \\\"string\\\" with escape \\n \\r \\f \\t \\v \\? \\u9829'",
        "It's a quoted \"string\" with escape \n \r \f \t \u000b ? \u9829");
  }

  @Test public void testReadQuotedStringWithOctEscape() {
    assertReadQuotedString("'\\040b'", "\040b");
    assertReadQuotedString("'\\40b'", "\040b");
    assertReadQuotedString("'\\401b'", "\0401b");
    assertReadQuotedString("'\\491b'", "\0491b");
    assertReadQuotedString("'\\0220\\022'", "\0220\022");
  }

  private void assertReadQuotedString(String source, String expect) {
    CharSource cs = createCharSource(source);
    char c = cs.read();  // skip first quote
    assertEquals(expect, cs.readQuotedString(c));
  }

  @Test public void testReadQuotedStringError() {
    CharSource cs = createCharSource("'Missing closing quote");
    char c = cs.read();  // skip first quote
    try {
      cs.readQuotedString(c);
      fail("Should throw error");
    } catch(EOFRuntimeException e) {
      assertEquals("Can't find matching quote at position:1", e.getMessage());
      // e.printStackTrace();
    }
  }
}
