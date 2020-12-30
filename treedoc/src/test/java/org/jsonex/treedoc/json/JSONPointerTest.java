package org.jsonex.treedoc.json;

import org.jsonex.treedoc.TDPath;
import org.jsonex.treedoc.TDPath.Part;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONPointerTest {
  @Test public void testParse() {
    verify("//ab.c/p1#/p1", new TDPath().setDocPath("//ab.c/p1").addParts(Part.ofRoot(), Part.ofChild("p1")));
    verify("//ab.c/p1#p1/p2", new TDPath().setDocPath("//ab.c/p1").addParts(Part.ofChildOrId("p1", "p1"), Part.ofChild("p2")));
    verify("1/p1", new TDPath().addParts(Part.ofRelative(1), Part.ofChild("p1")));
    verify("#../p1", new TDPath().addParts(Part.ofRelative(1), Part.ofChild("p1")));
    verify("1/p1", new TDPath().addParts(Part.ofRelative(1), Part.ofChild("p1")));
    verify("#./p1", new TDPath().addParts(Part.ofRelative(0), Part.ofChild("p1")));
  }

  private void verify(String str, TDPath expected) {
    assertEquals(expected, JSONPointer.get().parse(str));
  }
}
