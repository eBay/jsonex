package com.jsonex.csv;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true)
public class CSVOption {
  char fieldSep = ',';
  char recordSep = '\n';
  char quoteChar = '"';

  @Getter(lazy = true) private final String fieldAndRecord = "" + fieldSep + recordSep;
  @Getter(lazy = true) private final String fieldSepStr = "" + fieldSep;
  @Getter(lazy = true) private final String quoteCharStr = "" + quoteChar;
  @Getter(lazy = true) private final String recordSepStr = "" + recordSep;
}
