package com.jsonex.treeedoc;

import com.jsonex.treedoc.TDPath;
import org.junit.Test;

import static com.jsonex.treedoc.TDPath.Part.*;
import static com.jsonex.treedoc.TDPath.parse;
import static org.junit.Assert.assertEquals;

public class TDPathTest {
  @Test public void testParse() {
    assertEquals(new TDPath().addParts(ofRoot(), ofChild("p1"), ofChild("p2")), parse("/p1/p2"));
    assertEquals(new TDPath().addParts(ofRelative(1), ofChild("p1"), ofChild("p2")), parse("../p1/p2"));
    assertEquals(new TDPath().addParts(ofRelative(0), ofChild("p1"), ofChild("p2")), parse("./p1/p2"));
    assertEquals(new TDPath().addParts(ofId("100")).addParts(ofChild("p1")).addParts(ofChild("p2")), parse("#100/p1/p2"));
    assertEquals(new TDPath().addParts(ofRoot()).addParts(ofChild("p1")).addParts(ofChild("p2")), parse("#/p1/p2"));
  }
}
