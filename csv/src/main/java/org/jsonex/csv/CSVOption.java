package org.jsonex.csv;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true)
public class CSVOption {
  boolean includeHeader = true;
  char fieldSep = ',';
  char recordSep = '\n';
  char quoteChar = '"';

  @Getter(value= AccessLevel.NONE) @Setter(value=AccessLevel.NONE) String _fieldAndRecord;
  @Getter(value= AccessLevel.NONE) @Setter(value=AccessLevel.NONE) String _fieldSepStr;
  @Getter(value= AccessLevel.NONE) @Setter(value=AccessLevel.NONE) String _quoteCharStr;
  @Getter(value= AccessLevel.NONE) @Setter(value=AccessLevel.NONE) String _recordSepStr;

  { buildTerms(); }

  public CSVOption setFieldSep(char fieldSep) {
    this.fieldSep = fieldSep;
    return buildTerms();
  }

  public CSVOption setRecordSep(char recordSep) {
    this.recordSep = recordSep;
    return buildTerms();
  }

  public CSVOption setQuoteChar(char quoteChar) {
    this.quoteChar = quoteChar;
    return buildTerms();
  }

  private CSVOption buildTerms() {
    _fieldAndRecord = "" + fieldSep + recordSep;
    _fieldSepStr = "" + fieldSep;
    _quoteCharStr = "" + quoteChar;
    _recordSepStr = "" + recordSep;
    return this;
  }
}
