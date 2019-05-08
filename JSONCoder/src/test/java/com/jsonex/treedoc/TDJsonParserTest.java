package com.jsonex.treedoc;

import com.jsonex.treedoc.TDJSONWriter.JSONOption;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Reader;

import static junit.framework.Assert.assertEquals;

@Slf4j
public class TDJsonParserTest {
  @Test public void testParse() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.json");
    TDNode node = TDJSONParser.getInstance().parse(reader);

    log.info("Node=" + TestUtil.toJSON(node));
    assertEquals(10, node.getChild("limit").value);
    assertEquals("100000000000000000000", node.getChild("total").value);
    assertEquals("Some Name 1", node.getChildByPath("data/0/name").value);
    assertEquals("2nd st", node.getChildByPath("data/1/address/streetLine").value);

    String json = TDJSONWriter.getInstance().writeAsString(node);
    log.info("json: " + json);

    TDNode node1 = TDJSONParser.getInstance().parse(json);
    assertEquals(node, node1);

    json = TDJSONWriter.getInstance().writeAsString(node, new JSONOption().setIndentFactor(2));
    log.info("formatted json: " + json);
  }
}
