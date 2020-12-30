package org.jsonex.treedoc.json;

import org.jsonex.treedoc.TDNode;
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.util.FileUtil;
import org.jsonex.core.util.ListUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Slf4j
public class TDJsonWriterTest {
  private final static String MASKED = "[masked]";
  @Test public void testWriterWithValueMapper() {
    TDNode node = TDJSONParser.get().parse(FileUtil.readResource(this.getClass(), "testdata.json"));
    TDJSONOption opt = TDJSONOption.ofIndentFactor(2)
        .setValueMapper(n -> ListUtil.isIn(n.getKey(), "ip")  ? MASKED : n.getValue())
        .setNodeMapper(n -> ListUtil.isIn(n.getKey(), "address") ? new TDNode(n, n.getKey()).setValue(MASKED) : n);
    String str = TDJSONWriter.get().writeAsString(node, opt);
    log.info("testWriterWithValueMapper: str=\n" + str);
    node = TDJSONParser.get().parse(str);
    assertEquals(MASKED, node.getValueByPath("/data/0/ip"));
    assertEquals(MASKED, node.getValueByPath("/data/0/address"));
  }
}
