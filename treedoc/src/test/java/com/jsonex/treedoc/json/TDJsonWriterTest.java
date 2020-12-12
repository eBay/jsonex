package com.jsonex.treedoc.json;

import com.jsonex.core.util.FileUtil;
import com.jsonex.treedoc.TDNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Reader;

import static com.jsonex.core.util.ListUtil.in;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TDJsonWriterTest {
  private final static String MASKED = "[masked]";
  @Test public void testWriterWithValueMapper() {
    Reader reader = FileUtil.loadResource(this.getClass(), "testdata.json");
    TDNode node = TDJSONParser.get().parse(reader);
    TDJSONOption opt = TDJSONOption.withIndentFactor(2)
        .setValueMapper(n -> in(n.getKey(), "ip")  ? MASKED : n.getValue())
        .setNodeMapper(n -> in(n.getKey(), "address") ? new TDNode(n, n.getKey()).setValue(MASKED) : n);
    String str = TDJSONWriter.get().writeAsString(node, opt);
    log.info("testWriterWithValueMapper: str=\n" + str);
    node = TDJSONParser.get().parse(str);
    assertEquals(MASKED, node.getValueByPath("/data/0/ip"));
    assertEquals(MASKED, node.getValueByPath("/data/0/address"));
  }
}
