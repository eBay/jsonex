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
  public TDNode parse(CharSource source, CSVOption opt) { return parse(source, opt, new TreeDoc(null).getRoot()); }
  public TDNode parse(CharSource source, CSVOption opt, TDNode root) {
    root.setType(TDNode.Type.ARRAY);
    while(!source.isEof()) {
      if(!source.skipChars(SPACE_CHARS))
        break;
      TDNode child = root.createChild();
      readRecord(source, opt, child);
    }
    return root;
  }

  void readRecord(CharSource source, CSVOption opt, TDNode row) {
    row.setStart(source.getBookmark());
    while(!source.isEof() && source.peek() != opt.recordSep) {
      if (!source.skipChars(SPACE_CHARS))
        return;
      Bookmark start = source.getBookmark();
      TDNode fieldNode = row.createChild().setValue(readField(source, opt));
      fieldNode.setStart(start).setEnd(source.getBookmark());
    }
    row.setEnd(source.getBookmark());
    if (!source.isEof())
      source.read();  // Skip the recordSep
  }

  String readField(CharSource source, CSVOption opt) {
    StringBuilder sb = new StringBuilder();
    boolean previousQuoted = false;
    while(!source.isEof() && source.peek() != opt.fieldSep && source.peek() != opt.recordSep) {
      if (source.peek() == opt.quoteChar) {
        if (previousQuoted)
          sb.append(opt.quoteChar);
        source.skip();  // for "", we will keep one quote
        source.readUntil(opt.getQuoteCharStr(), sb);
        if (source.peek() == opt.quoteChar)
          source.skip();
        previousQuoted = true;
      } else {
        sb.append(source.readUntil(opt.getFieldAndRecord()).trim());
        previousQuoted = false;
      }
    }
    if (!source.isEof() && source.peek() == opt.fieldSep)
      source.skip();  // Skip fieldSep

    return sb.toString();
  }
}
