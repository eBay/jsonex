package org.jsonex.treedoc.json;

import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.util.FileUtil;
import org.jsonex.treedoc.TDNode;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class TDJsonWriterTest {
  @Test public void testWriterWithNodeFilter() {
    TDNode node = TDJSONParser.get().parse(readResource("testdata.json"));
    TDJSONOption opt = TDJSONOption.ofIndentFactor(2)
        .addNodeFilter(NodeFilter.mask(".*/address", ".*/ip"))
        .addNodeFilter(NodeFilter.exclude(".*/\\$id"));
    String str = TDJSONWriter.get().writeAsString(node, opt);
    log.info("testWriterWithNodeFilter: str=\n" + str);
    node = TDJSONParser.get().parse(str);
    assertNull(node.getValueByPath("/data/0/$id"));
    assertEquals("[Masked:len=10,aac1cfe2]", node.getValueByPath("/data/0/ip"));
    assertEquals("{Masked:len=2}", node.getValueByPath("/data/0/address"));  }

  @Test public void testQuote() {
    TDNode node = TDJSONParser.get().parse(readResource( "testQuote.json"));
    TDJSONOption opt = TDJSONOption.ofIndentFactor(2).setQuoteChars("\"'").setAlwaysQuoteKey(false).setAlwaysQuoteValue(false);
    String result = TDJSONWriter.get().writeAsString(node, opt) + "\n";
    assertEquals(readResource("testQuote_result.json"), result);
  }

  @Test public void testWriterWithTextDeco() {
    TDNode node = TDJSONParser.get().parse(readResource("testdata.json"));
    TDJSONOption opt = TDJSONOption.ofIndentFactor(2)
        .setTextDecorator((str, type) -> {
          switch (type) {
            case KEY: return "<b>" + str + "</b>";
            case OPERATOR: return "<font color=red>" + str + "</font>";
            case NON_STRING: return "<font color=green>" + str + "</font>";
            case STRING:
            default:
              return str;
          }
        });
    String str = "<pre>\n" + TDJSONWriter.get().writeAsString(node, opt) + "\n</pre>\n";
    assertEquals(readResource("testData_withTextDeco.html"), str);
  }

  @Test public void testWriterWithTypeWrapper() {
    TDNode node = TDJSONParser.get().parse(readResource("testQuote.json"));
    TDJSONOption opt = TDJSONOption.ofIndentFactor(2).setUseTypeWrapper(true);
    String str = TDJSONWriter.get().writeAsString(node, opt) + "\n";
    assertEquals(readResource("testData_withTypeWrapper.json"), str);
  }

  private String readResource(String fileName) {
    return FileUtil.readResource(this.getClass(), fileName);
  }
}
