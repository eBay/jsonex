package com.jsonex.cliarg;

import com.jsonex.core.annotation.*;
import com.jsonex.jsoncoder.JSONCoder;
import com.jsonex.jsoncoder.JSONCoderOption;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import static com.jsonex.snapshottest.Snapshot.assertMatchSnapshot;


@Slf4j
public class CliParserTest {
  @Data
  public static class Point {
    String name;
    int x;
    int y;
  }

  public enum Opt{ VAL1, VAL2 }

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

    @Index(2) @Description("number parameter") @Required(false)
    Point point;

    @ShortName("o") @Description( "Opt")
    Opt opt = Opt.VAL1;

    @ShortName("i")
    int optInt = 10;
  }

  @Test
  public void testParse() {
    CLISpec spec = new CLISpec(Arg1.class);
    log.info("usage: " + spec.printUsage());
    assertMatchSnapshot("usage", spec.printUsage());

    java.lang.String[] args = { "abc", "10", "name:n1,x:1,y:2", "-o", "VAL2", "--optInt", "100"};
    CLIParser parser = spec.parse(args, 0);
    log.info("cli:" + JSONCoder.encode(parser.target, JSONCoderOption.ofIndentFactor(2)));
    assertMatchSnapshot("parserTarget", parser.target);
  }
}
