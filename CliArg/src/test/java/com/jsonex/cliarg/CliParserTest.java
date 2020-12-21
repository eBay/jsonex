package com.jsonex.cliarg;

import com.jsonex.core.annotation.*;
import com.jsonex.jsoncoder.JSONCoder;
import com.jsonex.jsoncoder.JSONCoderOption;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    // TODO: implement snapshot test
    assertEquals("NAME: TestArg1\n" +
        "SUMMARY: This is a test arg1\n" +
        "DESCRIPTION\n" +
        "  Description of test Args\n" +
        "USAGE\n" +
        "  TestArg1 [-o <value>] [-i <value>] <strParam> <numParam> [point]\n" +
        "EXAMPLES\n" +
        "  arg1 2\n" +
        "  arg1 4\n" +
        "ARGUMENTS / OPTIONS\n" +
        "  <strParam>:  Str parameter\n" +
        "  <numParam>:  number parameter\n" +
        "  <point>:  number parameter\n" +
        "  -o, --opt:  Opt\n" +
        "  -i, --optInt:  ", spec.printUsage());
    log.info("usage: " + spec.printUsage());

    java.lang.String[] args = { "abc", "10", "name:n1,x:1,y:2", "-o", "VAL2", "--optInt", "100"};
    CLIParser parser = spec.parse(args, 0);
    log.info("cli:" + JSONCoder.encode(parser.target, JSONCoderOption.ofIndentFactor(2)));
    assertEquals("{\n" +
        "  \"strParam\":\"abc\",\n" +
        "  \"numParam\":10,\n" +
        "  \"point\":{\n" +
        "    \"name\":\"n1\",\n" +
        "    \"x\":1,\n" +
        "    \"y\":2\n" +
        "  },\n" +
        "  \"opt\":\"VAL2\",\n" +
        "  \"optInt\":100\n" +
        "}", JSONCoder.encode(parser.target, JSONCoderOption.ofIndentFactor(2)));
  }
}
