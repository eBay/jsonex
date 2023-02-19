package org.jsonex.csv;

import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.charsource.ArrayCharSource;
import org.jsonex.core.charsource.EOFRuntimeException;
import org.jsonex.core.charsource.ParseRuntimeException;
import org.jsonex.core.util.FileUtil;
import org.jsonex.treedoc.TDNode;
import org.junit.Test;

import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;
import static org.junit.Assert.assertEquals;


@Slf4j
public class CSVTest {
  @Test public void testParseAndWriterWithoutHeader() {
    CSVOption opt = new CSVOption().setIncludeHeader(false);
    TDNode node = CSVParser.get().parse(FileUtil.loadResource(CSVTest.class, "test.csv"), opt);
    assertMatchesSnapshot("parsed", node.toString());
    String str = CSVWriter.get().writeAsString(node, opt.setFieldSep('|'));
    assertMatchesSnapshot("asString", str);
    TDNode node1 = CSVParser.get().parse(str, opt);
    assertEquals(node, node1);
  }

  @Test public void testParseAndWriterWithHeader() {
    CSVOption opt = new CSVOption();
    TDNode node = CSVParser.get().parse(FileUtil.loadResource(CSVTest.class, "test.csv"), opt);
    assertMatchesSnapshot("parsed", node.toString());
    String str = CSVWriter.get().writeAsString(node, opt.setFieldSep('|'));
    assertMatchesSnapshot("asString", str);
    TDNode node1 = CSVParser.get().parse(str, opt);
    assertEquals(node, node1);
  }

  @Test public void testReadField() {
    assertEquals("ab'cd", CSVParser.get().readField(new ArrayCharSource("'ab''cd'"),
        new CSVOption().setQuoteChar('\'').buildTerms()));
  }

  @Test public void testReadFieldMissingQuote() {
    String error = "";
    try {
      CSVParser.get().readField(new ArrayCharSource("'ab''cd"), new CSVOption().setQuoteChar('\'').buildTerms());
    } catch (ParseRuntimeException e) {
      error = e.getMessage();
    }
    assertEquals("Can't find matching quote at position:5;line:0;col:5, Bookmark(line=0, col=7, pos=7), digest:", error);
  }
}
