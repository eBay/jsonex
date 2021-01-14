package org.jsonex.treedoc.json;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jsonex.treedoc.TDNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.jsonex.core.util.StringUtil.padEnd;

@Accessors(chain = true) @Data
public class TDJSONOption {
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

  // Used for JSONParser
  /** In case there's no enclosed '[' of '{' on the root level, the default type. */
  TDNode.Type defaultRootType = TDNode.Type.SIMPLE;

  // Used for JSONWriter
  int indentFactor;
  boolean alwaysQuoteName = true;
  char quoteChar = '"';
  String indentStr = "";

  /** Node mapper, if it returns null, node will be skipped */
  List<NodeFilter> nodeFilters = new ArrayList<>();

  public static TDJSONOption ofIndentFactor(int factor) { return new TDJSONOption().setIndentFactor(factor); }
  public static TDJSONOption ofDefaultRootType(TDNode.Type type) { return new TDJSONOption().setDefaultRootType(type); }

  public TDJSONOption setIndentFactor(int _indentFactor) {
    this.indentFactor = _indentFactor;
    indentStr = padEnd("", indentFactor);
    return this;
  }

  public boolean hasIndent() { return !indentStr.isEmpty(); }

  public TDNode applyFilters(TDNode n) {
    for (NodeFilter f : nodeFilters)
      n = f.apply(n);
    return n;
  }

  public TDJSONOption addNodeFilter(NodeFilter... filters) {
    for (NodeFilter f : filters)
      this.nodeFilters.add(f);
    return this;
  }
}
