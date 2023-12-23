package org.jsonex.core.type;

import org.junit.Test;

import java.util.Date;
import static org.junit.Assert.assertEquals;

public class TupleTest {
  @Test
  public void testTuple() {
    Tuple.Tuple3<String, Date, Integer> tpl = Tuple.of("string", new Date(), 100);
    assertEquals("string", tpl._0);
  }
}
