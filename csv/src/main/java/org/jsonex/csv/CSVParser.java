package org.jsonex.csv;

import org.jsonex.core.charsource.*;
import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.ClassUtil;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.TreeDoc;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jsonex.core.util.ListUtil.map;

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
    opt.buildTerms();
    List<String> fields = null;
    root.setType(TDNode.Type.ARRAY);
    if (opt.includeHeader) {
      fields = map(readNonEmptyRecord(src, opt), Object::toString);
      if (fields.isEmpty())
        return root;
      if (fields.get(0) == TDNode.COLUMN_KEY)
        root.setType(TDNode.Type.MAP);
    }

    while (!src.isEof()) {
      if (!src.skipChars(SPACE_CHARS))
        break;
      readRecord(src, opt, root, fields);
    }
    return root;
  }

  void readRecord(CharSource src, CSVOption opt, TDNode root, List<String> fields) {
    TDNode row = new TDNode(root.getDoc(), null).setType(fields == null ? TDNode.Type.ARRAY: TDNode.Type.MAP);
    row.setStart(src.getBookmark());
    int i = 0;
    while (!src.isEof() && src.peek() != opt.recordSep) {
      if (!src.skipChars(SPACE_CHARS))
        break;
      Bookmark start = src.getBookmark();
      Object val = readField(src, opt);
      String key = null;
      if (fields != null) {
        if (i >= fields.size())
          throw src.createParseRuntimeException("The row has more columns than headers");
        key = fields.get(i++);
        if (key == TDNode.COLUMN_KEY) {
          row.setKey(key);
          continue;
        }
      }
      TDNode field = row.createChild(key).setValue(val);
      field.setStart(start).setEnd(src.getBookmark());
    }
    row.setEnd(src.getBookmark());
    if (row.hasChildren())
      root.addChild(row);
    if (!src.isEof())
      src.read();  // Skip the recordSep
  }

  public List<Object> readNonEmptyRecord(CharSource src, CSVOption opt) {
    while(!src.isEof()) {
      List<Object> res = readRecord(src, opt);
      if (!res.isEmpty())
        return res;
    }
    return Collections.emptyList();
  }

  public List<Object> readRecord(CharSource src, CSVOption opt) {
    List<Object> result = new ArrayList<>();
    while (!src.isEof() && src.peek() != opt.recordSep) {
      if (!src.skipChars(SPACE_CHARS))
        break;
      result.add(readField(src, opt));
    }
    if (!src.isEof())
      src.read();  // Skip the recordSep
    return result;
  }

  Object readField(CharSource src, CSVOption opt) {
    StringBuilder sb = new StringBuilder();
    if (src.isEof())
      return sb.toString();

    boolean isString = false;
    if (src.peek() != opt.quoteChar) {  // Read non-quoted string
      sb.append(src.readUntil(opt._fieldAndRecord).trim());
    } else {  // Read quoted string
      isString = true;
      src.skip();
      while (!src.isEof() && src.peek() != opt.fieldSep && src.peek() != opt.recordSep) {
        // Not calling getBookmark() to avoid clone an object
        int pos = src.bookmark.getPos();
        int line = src.bookmark.getLine();
        int col = src.bookmark.getCol();

        src.readUntil(sb, opt._quoteCharStr);
        if (src.isEof())
          throw src.createParseRuntimeException("Can't find matching quote at position:" + pos + ";line:" + line + ";col:" + col);

        src.skip();
        if (src.isEof())
          break;
        if (src.peek() == opt.quoteChar) {
          sb.append(opt.quoteChar);
          src.skip();
        } else {
          break;
        }
      }
      src.skipSpacesAndReturns();
    }

    if (!src.isEof() && src.peek() == opt.fieldSep)
      src.skip();  // Skip fieldSep

    String str = sb.toString();
    return isString ? str : ClassUtil.toSimpleObject(str);
  }
}
