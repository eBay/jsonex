package com.jsonex.treedoc;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Reader;

@Accessors(chain = true)
public class TDJSONParserOption {
  /** The source */
  @Getter @Setter
  CharSource source;

  /** In case there's no enclosed '[' of '{' on the root level, the default type. */
  @Getter @Setter
  TDNode.Type defaultRootType = TDNode.Type.SIMPLE;

  /** Set source with a reader */
  public TDJSONParserOption setReader(Reader reader) {
    source = reader == null ? null : new ReaderCharSource(reader);
    return this;
  }

  /** Set source of a json string */
  public TDJSONParserOption setJson(String jsonStr) {
    source = jsonStr == null ? null : new ArrayCharSource(jsonStr.toCharArray());
    return this;
  }

  public static TDJSONParserOption of(CharSource source) { return new TDJSONParserOption().setSource(source); }
  public static TDJSONParserOption of(String source) { return new TDJSONParserOption().setJson(source); }
  public static TDJSONParserOption of(Reader source) { return new TDJSONParserOption().setReader(source); }
}
