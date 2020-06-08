package com.jsonex.treedoc.json;

import com.jsonex.core.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class TDJSONWriterOption {
  @Getter int indentFactor;
  @Getter @Setter boolean alwaysQuoteName = true;
  @Getter @Setter char quoteChar = '"';
  @Getter transient String indentStr = "";  // Used internally

  public TDJSONWriterOption setIndentFactor(int _indentFactor) {
    this.indentFactor = _indentFactor;
    indentStr = StringUtil.appendRepeatedly(new StringBuilder(), ' ', indentFactor).toString();
    return this;
  }
}
