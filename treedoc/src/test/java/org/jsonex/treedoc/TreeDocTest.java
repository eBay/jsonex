package org.jsonex.treedoc;

import org.jsonex.core.util.FileUtil;
import org.jsonex.treedoc.json.TDJSONOption;
import org.jsonex.treedoc.json.TDJSONParser;
import org.jsonex.treedoc.json.TDJSONWriter;
import org.junit.Test;

import static org.jsonex.core.util.FileUtil.readResource;
import static org.junit.Assert.assertEquals;

public class TreeDocTest {
  @Test public void testDedupeNodes() {
    TDNode node = TDJSONParser.get().parse(readResource(getClass(), "test.json"));
    node.getDoc().dedupeNodes();
    String result = TDJSONWriter.get().writeAsString(node, TDJSONOption.ofIndentFactor(2).setAlwaysQuoteName(false));
    assertEquals(FileUtil.readResource(getClass(), "test_deduped.json"), result);
  }
}
