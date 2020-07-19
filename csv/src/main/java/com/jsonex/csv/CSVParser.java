package com.jsonex.csv;

import com.jsonex.core.charsource.ArrayCharSource;
import com.jsonex.core.charsource.Bookmark;
import com.jsonex.core.charsource.CharSource;
import com.jsonex.core.charsource.ReaderCharSource;
import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.treedoc.TDNode;
import com.jsonex.treedoc.TreeDoc;

import java.io.Reader;

public class CSVParser {
  private static String SPACE_CHARS = " \r";
  public final static InjectableInstance<CSVParser> instance = InjectableInstance.of(CSVParser.class);
  public static CSVParser get() { return instance.get(); }

  public TDNode parse(String str) { return parse(str, new CSVOption()); }
  public TDNode parse(Reader reader) { return parse(reader, new CSVOption()); }

  public TDNode parse(Reader reader, CSVOption opt) { return parse(new ReaderCharSource(reader), opt); }
  public TDNode parse(String str, CSVOption opt) { return parse(new ArrayCharSource(str), opt); }
  public TDNode parse(CharSource src, CSVOption opt) { return parse(src, opt, new TreeDoc(null).getRoot()); }
  public TDNode parse(CharSource src, CSVOption opt, TDNode root) {
    root.setType(TDNode.Type.ARRAY);
    while (!src.isEof()) {
      if (!src.skipChars(SPACE_CHARS))
        break;
      readRecord(src, opt, root);
    }
    return root;
  }

  void readRecord(CharSource src, CSVOption opt, TDNode root) {
    TDNode row = new TDNode(root.getDoc(), null);
    row.setStart(src.getBookmark());
    while (!src.isEof() && src.peek() != opt.recordSep) {
      if (!src.skipChars(SPACE_CHARS))
        break;
      Bookmark start = src.getBookmark();
      TDNode field = row.createChild().setValue(readField(src, opt));
      field.setStart(start).setEnd(src.getBookmark());
    }
    row.setEnd(src.getBookmark());
    if (row.hasChildren())
      root.addChild(row);
    if (!src.isEof())
      src.read();  // Skip the recordSep

  }

  String readField(CharSource src, CSVOption opt) {
    StringBuilder sb = new StringBuilder();
    boolean previousQuoted = false;
    while (!src.isEof() && src.peek() != opt.fieldSep && src.peek() != opt.recordSep) {
      if (src.peek() == opt.quoteChar) {
        if (previousQuoted)
          sb.append(opt.quoteChar);
        src.skip();  // for "", we will keep one quote
        src.readUntil(opt.getQuoteCharStr(), sb);
        if (src.peek() == opt.quoteChar)
          src.skip();
        previousQuoted = true;
      } else {
        sb.append(src.readUntil(opt.getFieldAndRecord()).trim());
        previousQuoted = false;
      }
    }
    if (!src.isEof() && src.peek() == opt.fieldSep)
      src.skip();  // Skip fieldSep

    return sb.toString();
  }
}
