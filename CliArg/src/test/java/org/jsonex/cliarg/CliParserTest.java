package org.jsonex.cliarg;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.annotation.*;
import org.junit.Test;

import static org.jsonex.snapshottest.Snapshot.assertMatchesSnapshot;
import static org.junit.Assert.assertTrue;


@Slf4j
public class CliParserTest {
  @Data
  public static class Point {
    String name;
    int x;
    int y;
  }

  public enum Opt { VAL1, VAL2 }

  @Name("TestArg1") @Summary("This is a test arg1") @Description("Description of test Args") @Data
  @Examples({
      "arg1 2",
      "arg1 4",
  })
  public static class Arg1 {
    @Index(0) @Description("Str parameter")
    String strParam;

    @Index(1) @Description("number parameter")
    int numParam;

    @Index(2) @Description("Object Point") @Required(false)
    Point point;

    @ShortName("o") @Description( "Opt") @Required(true)
    Opt opt = Opt.VAL1;

    @ShortName("i")
    int optInt = 10;
  }

  @Test
  public void testParse() {
    CLISpec spec = new CLISpec(Arg1.class);
    assertMatchesSnapshot("spec", spec);

    log.info("spec:\n" + spec.printUsage());
    assertMatchesSnapshot("usage", spec.printUsage());

    String[] args = { "abc", "10", "name:n1,x:1,y:2", "-o", "VAL2", "--optInt", "100"};
    CLIParser parser = spec.parse(args, 0);
    log.info("parsedValue:\n" + parser.target);
    assertMatchesSnapshot("parserTarget", parser.target);

    String[] argsWithError = { "-o", "VAL2", "--optInt1"};
    parser = spec.parse(argsWithError, 0);
    assertTrue(parser.hasError());
    assertMatchesSnapshot("parseError", parser.getErrorsAsString());
  }
}
