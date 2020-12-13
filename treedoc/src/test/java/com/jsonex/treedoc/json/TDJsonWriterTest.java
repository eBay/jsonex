package com.jsonex.treedoc.json;

import com.jsonex.treedoc.TDNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.jsonex.core.util.FileUtil.readResource;
import static com.jsonex.core.util.ListUtil.in;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TDJsonWriterTest {
  private final static String MASKED = "[masked]";
  @Test public void testWriterWithValueMapper() {
    TDNode node = TDJSONParser.get().parse(readResource(this.getClass(), "testdata.json"));
    TDJSONWriterOption opt = TDJSONWriterOption.withIndentFactor(2)
        .setValueMapper(n -> in(n.getKey(), "ip")  ? MASKED : n.getValue())
        .setNodeMapper(n -> in(n.getKey(), "address") ? new TDNode(n, n.getKey()).setValue(MASKED) : n);
    String str = TDJSONWriter.get().writeAsString(node, opt);
    log.info("testWriterWithValueMapper: str=\n" + str);
    node = TDJSONParser.get().parse(str);
    assertEquals(MASKED, node.getValueByPath("/data/0/ip"));
    assertEquals(MASKED, node.getValueByPath("/data/0/address"));
  }
}
