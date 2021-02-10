package org.jsonex.treedoc.json;

import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.util.FileUtil;
import org.jsonex.treedoc.TDNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Slf4j
public class TDJsonWriterTest {
  @Test public void testWriterWithValueMapper() {
    TDNode node = TDJSONParser.get().parse(FileUtil.readResource(this.getClass(), "testdata.json"));
    TDJSONOption opt = TDJSONOption.ofIndentFactor(2)
        .addNodeFilter(NodeFilter.mask(".*/address", ".*/ip"))
        .addNodeFilter(NodeFilter.exclude(".*/\\$id"));
    String str = TDJSONWriter.get().writeAsString(node, opt);
    log.info("testWriterWithValueMapper: str=\n" + str);
    node = TDJSONParser.get().parse(str);
    assertNull(node.getValueByPath("/data/0/$id"));
    assertEquals("<Masked:len=10>", node.getValueByPath("/data/0/ip"));
    assertEquals("{Masked:size=2}", node.getValueByPath("/data/0/address"));
  }
}
