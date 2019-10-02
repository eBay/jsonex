package com.jsonex.treedoc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Reader;

import static com.jsonex.treedoc.TDNode.Type.ARRAY;
import static com.jsonex.treedoc.TDNode.Type.MAP;
import static org.junit.Assert.*;

@Slf4j
public class TDJsonParserTest {
  @Test public void testSkipSpaceAndComments() {
    ArrayCharSource in = new ArrayCharSource("  //abcd \n // defghi \n abc");
    assertTrue(TDJSONParser.skipSpaceAndComments(in));
    assertEquals("abc", in.read(3));

    in = new ArrayCharSource("  #abcd \n # defghi \n abc");
    assertTrue(TDJSONParser.skipSpaceAndComments(in));
    assertEquals("abc", in.read(3));

    in = new ArrayCharSource("/* abcd*/ \n /* defghi*/ \n abc");
    assertTrue(TDJSONParser.skipSpaceAndComments(in));
    assertEquals("abc", in.read(3));
  }


  @Test public void testParse() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.json");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader));

    log.info("Node=" + TestUtil.toJSON(node));
    assertEquals(10, node.getChild("limit").value);
    assertEquals("100000000000000000000", node.getChild("total").value);
    assertEquals("Some Name 1", node.getChildByPath("data/0/name").value);
    assertEquals("2nd st", node.getChildByPath("data/1/address/streetLine").value);

    String json = TDJSONWriter.get().writeAsString(node);
    log.info("json: " + json);

    TDNode node1 = TDJSONParser.get().parse(new TDJSONParserOption(json));
    assertEquals(node, node1);

    json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("formatted json: " + json);
  }

  @Test public void testParseProto() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.textproto");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader).setDefaultRootType(MAP));
    log.info("testParseProto: Node=" + TestUtil.toJSON(node));
    String json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("testParseProto: formatted json: " + json);
    assertEquals(Boolean.FALSE, node.getChildByPath("n/n1/0/n11/1/n111").getValue());
    assertEquals(4, node.getChildByPath("n/n1/1/[d.e.f]").getValue());
    assertEquals(6, node.getChildByPath("n/n3/0").getValue());
  }


  @Test public void testParseJson5() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.json5");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader).setDefaultRootType(MAP));
    log.info("testParseJson5: Node=" + TestUtil.toJSON(node));
    String json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("testParseJson5: formatted json: " + json);
    assertEquals("and you can quote me on that", node.getChildByPath("unquoted").getValue());
    assertEquals(912559, node.getChildByPath("hexadecimal").getValue());
    assertEquals(0.8675309, node.getChildByPath("leadingDecimalPoint").getValue());
    assertEquals(1, node.getChildByPath("positiveSign").getValue());
  }

  @Test public void testRootArray() {
    Reader reader = TestUtil.loadResource(this.getClass(), "rootArray.json");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader).setDefaultRootType(ARRAY));
    log.info("testParseJson5: Node=" + TestUtil.toJSON(node));
    String json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("testParseJson5: formatted json: " + json);
    assertEquals(4, node.getChildrenSize());
    assertEquals(2, node.getChildByPath("1").getValue());
    assertEquals(3, node.getChildByPath("2/v").getValue());
  }

  @Test public void testInvalid() {
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption("}"));
    assertEquals("}", node.value);

    node = TDJSONParser.get().parse(new TDJSONParserOption(""));
    assertNull(node.value);
  }
}
