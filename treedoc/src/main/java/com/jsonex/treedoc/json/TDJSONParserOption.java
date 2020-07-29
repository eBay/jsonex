package com.jsonex.treedoc.json;

import com.jsonex.treedoc.TDNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.net.URI;

@Accessors(chain = true) @RequiredArgsConstructor @Data()
public class TDJSONParserOption {
  String KEY_ID = "$id";
//  String KEY_REF = "$ref";
//  String ObJ_START = "{";
//  String ObJ_END = "}";
//  String ARRAY_START = "[";
//  String ARRAY_END = "]";
//  String VAL_SEP = ":";
//  String VAL_END = ",";

  /** The source */
  //final CharSource source;
  URI uri;

  /** In case there's no enclosed '[' of '{' on the root level, the default type. */
  TDNode.Type defaultRootType = TDNode.Type.SIMPLE;

  /** Set source with a reader */
  //public TDJSONParserOption(Reader reader) { this(new ReaderCharSource(reader)); }

  /** Set source with a reader */
//  @SneakyThrows
//  public static TDJSONParserOption of(URI uri) {
//    if (uri.getHost() == null) {
//      try (Reader reader = new FileReader(uri.getPath())) {
//        return new TDJSONParserOption(new ReaderCharSource(reader)).setUri(uri);
//      }
//    } else {
//      try (Reader reader = new InputStreamReader(uri.toURL().openStream())) {
//        return new TDJSONParserOption(new ReaderCharSource(reader)).setUri(uri);
//      }
//    }
//  }

//  /** Set source of a json string */
//  public TDJSONParserOption(String jsonStr) { source = new ArrayCharSource(jsonStr.toCharArray()); }
}
