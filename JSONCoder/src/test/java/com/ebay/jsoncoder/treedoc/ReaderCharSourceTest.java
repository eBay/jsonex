package com.ebay.jsoncoder.treedoc;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ReaderCharSourceTest extends BasecharSourceTest {
  protected CharSource createCharSource(String str, int startIndex, int endIndex) {
    return new ReaderCharSource(new StringReader(str.substring(startIndex, endIndex)), 5);
  }

  @Test public void testCharArraySource1() {
    ReaderCharSource cs = new ReaderCharSource(new StringReader("0123456"), 3);
    assertEquals('0', cs.read());
    assertEquals('1', cs.peek(0));
    assertEquals('2', cs.peek(1));
    assertEquals('1', cs.read());
    assertEquals('2', cs.peek(0));
    assertEquals('3', cs.peek(1));
    assertEquals('2', cs.read());
    assertEquals('3', cs.read());
    assertEquals('4', cs.read());
    assertEquals('5', cs.read());
    try {
      cs.peek(1);
      assertFalse("Should throw EOF", false);
    } catch (EOFRuntimeException e) {
    }
    assertEquals('6', cs.read());
    assertEquals(7, cs.getPos());
    assertTrue("should eof", cs.isEof(0));
    try {
      cs.read();
      assertFalse("Should throw EOF", false);
    } catch (EOFRuntimeException e) {
    }
  }
}
