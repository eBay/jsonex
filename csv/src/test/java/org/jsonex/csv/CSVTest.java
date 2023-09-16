package org.jsonex.csv;

import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.charsource.ArrayCharSource;
import org.jsonex.core.charsource.ParseRuntimeException;
import org.jsonex.core.util.FileUtil;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.json.TDJSONParser;
import org.junit.Test;

import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;
import static org.junit.Assert.assertEquals;


@Slf4j
public class CSVTest {
  private void testParseAndWrite(CSVOption opt, String file) {
    TDNode node = CSVParser.get().parse(FileUtil.loadResource(CSVTest.class, file), opt);
    assertMatchesSnapshot("parsed", node.toString());
    String str = CSVWriter.get().writeAsString(node, opt.setFieldSep('|'));
    assertMatchesSnapshot("asString", str);
    TDNode node1 = CSVParser.get().parse(str, opt);
    assertEquals(node, node1);
  }

  @Test public void testParseAndWriteWithoutHeader() {
    testParseAndWrite(new CSVOption().setIncludeHeader(false), "test.csv");
  }

  @Test public void testParseAndWriteWithHeader() {
    testParseAndWrite(new CSVOption(), "test.csv");
  }

  @Test public void testParseAndWriteObj() {
    testParseAndWrite(new CSVOption(), "testObj.csv");
  }

  @Test public void testJSONValue() {
    String json = "[{f1: v1, f2: {a: 1， b: 2}}, {f2:'', f3: 3}]";
    assertMatchesSnapshot(CSVWriter.get().writeAsString(TDJSONParser.get().parse(json)));
  }

  @Test public void testReadField() {
    assertEquals("ab'cd", CSVParser.get().readField(new ArrayCharSource("'ab''cd'"),
        new CSVOption().setQuoteChar('\'')));
  }

  @Test public void testReadFieldMissingQuote() {
    String error = "";
    try {
      CSVParser.get().readField(new ArrayCharSource("'ab''cd"), new CSVOption().setQuoteChar('\''));
    } catch (ParseRuntimeException e) {
      error = e.getMessage();
    }
    assertEquals("Can't find matching quote at position:5;line:0;col:5, Bookmark(line=0, col=7, pos=7), digest:", error);
  }
}
