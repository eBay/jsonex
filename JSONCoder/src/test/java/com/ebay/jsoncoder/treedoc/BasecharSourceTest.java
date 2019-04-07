package com.ebay.jsoncoder.treedoc;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public abstract class BasecharSourceTest {
  protected abstract CharSource createCharSource(String str, int startIndex, int endIndex);
  protected CharSource createCharSource(String str) { return createCharSource(str, 0, str.length()); }

  @Test public void testCharArraySource() {
    CharSource cs = createCharSource("--0123--", 2, 6);
    assertEquals('0', cs.read());
    assertEquals('1', cs.read());
    assertEquals('2', cs.peek(0));
    assertEquals('3', cs.peek(1));
    assertEquals('2', cs.read());
    assertEquals('3', cs.read());
    assertTrue("should eof", cs.isEof(0));
    try {
      cs.read();
      assertFalse("Should throw EOF", false);
    } catch (EOFRuntimeException e) {
    }
  }

  @Test public void testParseText() {
    CharSource cs = createCharSource("  Text before /* some comments */ Text after");
    cs.skipSpaces();
    assertEquals(2, cs.getPos());

    StringBuilder target = new StringBuilder();
    assertTrue("should match /*", cs.readUntilMatch(1000, "/*", false, target));
    assertEquals("Text before ", target.toString());
    assertTrue("should start with /*", cs.startsWidth("/*"));
    cs.skip(2);  // skip /*

    target = new StringBuilder();
    assertTrue("should match with */", cs.readUntilMatch(1000, "*/", false, target));
    assertEquals(" some comments ", target.toString());

    target = new StringBuilder();
    assertTrue("should match with */", cs.readUntilMatch(1000, "*/", false, target));
  }

  @Test public void testReadQuotedString() {
    CharSource cs = createCharSource("'It\\'s a quoted \\\"string\\\" with escapte \\n \\r \\f \\t \\u9829'");
    char c = cs.read();  // skip first quote
    assertEquals("It's a quoted \"string\" with escapte \n \r \f \t \u9829", cs.readQuotedString(c));
  }

  @Test public void testReadQuotedStringError() {
    CharSource cs = createCharSource("'Missing closing quote");
    char c = cs.read();  // skip first quote
    try {
      cs.readQuotedString(c);
      fail("Should throw error");
    } catch(EOFRuntimeException e) {
      e.printStackTrace();
    }

    cs = createCharSource("`Invalid escape \\p abcdefg`");
    c = cs.read();  // skip first quote
    try {
      cs.readQuotedString(c);
      fail("Should throw error when parsing invalid escape");
    } catch(ParseRuntimeException e) {
      e.printStackTrace();
    }

  }
}
