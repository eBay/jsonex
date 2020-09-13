package com.jsonex.treedoc.json;

import com.jsonex.core.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class TDJSONOption {
  @Getter int indentFactor;
  @Getter @Setter boolean alwaysQuoteName = true;
  @Getter @Setter char quoteChar = '"';
  @Getter @Setter String indentStr = "";

  public static TDJSONOption withIndentFactor(int factor) { return new TDJSONOption().setIndentFactor(factor); }

  public TDJSONOption setIndentFactor(int _indentFactor) {
    this.indentFactor = _indentFactor;
    indentStr = StringUtil.appendRepeatedly(new StringBuilder(), ' ', indentFactor).toString();
    return this;
  }

  public boolean hasIndent() { return !indentStr.isEmpty(); }
}
