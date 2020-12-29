package com.jsonex.cliarg;

import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static com.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;

@Slf4j
public class TextFormatterTest {
  public @Rule TestName testName = new TestName();
  @Test public void testAlignTabs() {
    String str = "ab\tcdef\tghijk\n" +
        "a\tbcdefg\th\n" +
        "a\tb\tcde\tfg\th\n";
    assertMatchesSnapshot("alignTabs", TextFormatter.alignTabs(str));
  }

  @Test public void testIndent() {
    String str = "abcd\nefg\nhijk\n";
    assertMatchesSnapshot("indented", TextFormatter.indent(str, "  "));
  }
}
