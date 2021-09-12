package org.jsonex.csv;

import org.jsonex.core.charsource.*;
import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.TreeDoc;

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
    TDNode row = new TDNode(root.getDoc(), null).setType(TDNode.Type.ARRAY);
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

  Object readField(CharSource src, CSVOption opt) {
    StringBuilder sb = new StringBuilder();
    boolean previousQuoted = false;
    boolean isString = false;
    while (!src.isEof() && src.peek() != opt.fieldSep && src.peek() != opt.recordSep) {
      if (src.peek() == opt.quoteChar) {
        isString = true;
        if (previousQuoted)
          sb.append(opt.quoteChar);

        // Not calling getBookmark() to avoid clone an object
        int pos = src.bookmark.getPos();
        int line = src.bookmark.getLine();
        int col = src.bookmark.getCol();

        src.skip();  // for "", we will keep one quote
        src.readUntil(opt.getQuoteCharStr(), sb);

        if (src.isEof())
          throw new EOFRuntimeException("Can't find matching quote at position:" + pos + ";line:" + line + ";col:" + col);
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

    String str = sb.toString();
    return isString ? str : ClassUtil.toSimpleObject(str);
  }
}
