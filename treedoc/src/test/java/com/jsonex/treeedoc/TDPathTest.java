package com.jsonex.treeedoc;

import com.jsonex.treedoc.TDPath;
import com.jsonex.treedoc.TDPath.Part;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TDPathTest {
  @Test public void testParse() {
    assertEquals(new TDPath().addParts(Part.ofRoot()).addParts(Part.ofChild("p1")).addParts(Part.ofChild("p2")),
        TDPath.parse("/p1/p2"));
    assertEquals(new TDPath().addParts(Part.ofRelative(1)).addParts(Part.ofChild("p1")).addParts(Part.ofChild("p2")),
        TDPath.parse("../p1/p2"));
    assertEquals(new TDPath().addParts(Part.ofRelative(0)).addParts(Part.ofChild("p1")).addParts(Part.ofChild("p2")),
        TDPath.parse("./p1/p2"));
  }
}
