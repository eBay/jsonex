package com.jsonex.treedoc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Reader;

@Accessors(chain = true) @RequiredArgsConstructor
public class TDJSONParserOption {
  /** The source */
  @Getter final CharSource source;

  /** In case there's no enclosed '[' of '{' on the root level, the default type. */
  @Getter @Setter
  TDNode.Type defaultRootType = TDNode.Type.SIMPLE;

  /** Set source with a reader */
  public TDJSONParserOption(Reader reader) { source = new ReaderCharSource(reader); }

  /** Set source of a json string */
  public TDJSONParserOption(String jsonStr) { source = new ArrayCharSource(jsonStr.toCharArray()); }
}
