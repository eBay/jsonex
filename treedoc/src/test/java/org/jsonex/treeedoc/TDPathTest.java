package org.jsonex.treeedoc;

import org.jsonex.treedoc.TDPath;
import org.junit.Test;

import static org.jsonex.treedoc.TDPath.Part.*;
import static org.jsonex.treedoc.TDPath.parse;
import static org.junit.Assert.assertEquals;

public class TDPathTest {
  @Test public void testParse() {
//    assertEquals(TDPath.ofParts(ofRoot(), ofChild("p1"), ofChild("p2")), parse("/p1/p2"));
//    assertEquals(TDPath.ofParts(ofRelative(1), ofChild("p1"), ofChild("p2")), parse("../p1/p2"));
//    assertEquals(TDPath.ofParts(ofRelative(0), ofChild("p1"), ofChild("p2")), parse("./p1/p2"));
//    assertEquals(TDPath.ofParts(ofChildOrId("#100", "100"), ofChild("p1"), ofChild("p2")), parse("#100/p1/p2"));
//    assertEquals(TDPath.ofParts(ofRoot(), ofChild("p1"), ofChild("p2")), parse("#/p1/p2"));
    assertEquals(TDPath.ofParts(ofRelative(1)), parse("../"));
  }
}
