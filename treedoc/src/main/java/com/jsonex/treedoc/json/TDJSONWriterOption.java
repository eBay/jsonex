package com.jsonex.treedoc.json;

import com.jsonex.core.util.StringUtil;
import com.jsonex.treedoc.TDNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Function;

@Accessors(chain = true) @Data
public class TDJSONWriterOption {
  int indentFactor;
  boolean alwaysQuoteName = true;
  char quoteChar = '"';
  String indentStr = "";
  /** Node mapper, if it returns null, node will be skipped */
  Function<TDNode, TDNode> nodeMapper = Function.identity();
  Function<TDNode, Object> valueMapper = null;

  public static TDJSONWriterOption withIndentFactor(int factor) { return new TDJSONWriterOption().setIndentFactor(factor); }

  public TDJSONWriterOption setIndentFactor(int _indentFactor) {
    this.indentFactor = _indentFactor;
    indentStr = StringUtil.appendRepeatedly(new StringBuilder(), ' ', indentFactor).toString();
    return this;
  }

  public boolean hasIndent() { return !indentStr.isEmpty(); }
}
