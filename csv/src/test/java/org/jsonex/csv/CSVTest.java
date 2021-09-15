package org.jsonex.csv;

import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.charsource.ArrayCharSource;
import org.jsonex.core.charsource.EOFRuntimeException;
import org.jsonex.core.util.FileUtil;
import org.jsonex.treedoc.TDNode;
import org.junit.Test;

import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;
import static org.junit.Assert.assertEquals;


@Slf4j
public class CSVTest {
  @Test public void testParseAndWriter() {
    TDNode node = CSVParser.get().parse(FileUtil.loadResource(CSVTest.class, "test.csv"));
    assertMatchesSnapshot("parsed", node.toString());

    CSVOption opt = new CSVOption().setFieldSep('|');
    String str = CSVWriter.get().writeAsString(node, opt);
    assertMatchesSnapshot("asString", str);
    TDNode node1 = CSVParser.get().parse(str, opt);
    assertEquals(node, node1);
  }

  @Test public void testReadField() {
    assertEquals("ab'cd", CSVParser.get().readField(new ArrayCharSource("'ab''cd'"),
        new CSVOption().setQuoteChar('\'')));
  }

  @Test public void testReadFieldMissingQuote() {
    String error = "";
    try {
      CSVParser.get().readField(new ArrayCharSource("'ab''cd"), new CSVOption().setQuoteChar('\''));
    } catch (EOFRuntimeException e) {
      error = e.getMessage();
    }
    assertEquals("Can't find matching quote at position:4;line:0;col:4", error);
  }
}
