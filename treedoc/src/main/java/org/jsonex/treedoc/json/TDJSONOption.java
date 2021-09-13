package org.jsonex.treedoc.json;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jsonex.treedoc.TDNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static org.jsonex.core.util.StringUtil.padEnd;

@Accessors(chain = true) @Data
public class TDJSONOption {
  public enum TextType {OPERATOR, KEY, STRING, NON_STRING}
  String KEY_ID = "$id";
//  String KEY_REF = "$ref";
//  String ObJ_START = "{";
//  String ObJ_END = "}";
//  String ARRAY_START = "[";
//  String ARRAY_END = "]";
  String deliminatorKey = ":";
  String deliminatorValue = ",";

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
  BiFunction<String, TextType, String> textDecorator;

  /**
   * if this is set, all the id in $id and $ref will be suffixed with "_" + docId, this is to avoid collision when merge
   * multiple docs as stream
   */
  String docId = null;
  public TDJSONOption() { buildTerms(); }
  public TDJSONOption setDocId(int docId) { this.docId = "" + docId; return this; }

  /** Node filters, if it returns null, node will be skipped */
  List<NodeFilter> nodeFilters = new ArrayList<>();

  public static TDJSONOption ofIndentFactor(int factor) { return new TDJSONOption().setIndentFactor(factor); }
  public static TDJSONOption ofDefaultRootType(TDNode.Type type) { return new TDJSONOption().setDefaultRootType(type); }
  public static TDJSONOption ofMapToString() { return new TDJSONOption().setDeliminatorKey("=").setDeliminatorValue(", "); }

  public TDJSONOption setIndentFactor(int _indentFactor) {
    this.indentFactor = _indentFactor;
    indentStr = padEnd("", indentFactor);
    return this;
  }

  public boolean hasIndent() { return !indentStr.isEmpty(); }

  public TDJSONOption setDeliminatorKey(String val) { deliminatorKey = val; buildTerms(); return this; }
  public TDJSONOption setDeliminatorValue(String val) { deliminatorValue = val; buildTerms(); return this; }

  public TDNode applyFilters(TDNode n) {
    for (NodeFilter f : nodeFilters) {
      if (n == null)
        break;
      n = f.apply(n);
    }
    return n;
  }

  public TDJSONOption addNodeFilter(NodeFilter... filters) {
    for (NodeFilter f : filters)
      this.nodeFilters.add(f);
    return this;
  }

  // Helper methods for deco text
  public String deco(String text, TextType type) {
    return textDecorator == null ? text : textDecorator.apply(text, type);
  }

  // Package scopes used by parser
  String termValue;
  String termValueInMap;
  String termValueInArray;
  String termKey;
  Collection<String> termValueStrs;
  Collection<String> termKeyStrs;

  void buildTerms() {
    termValue = "\n\r";
    termKey = "{[}";
    termValueStrs = new ArrayList<>();
    termKeyStrs = new ArrayList<>();
    if (deliminatorValue.length() == 1) {  // If more than 1, will use separate string collection as term
      termValue += deliminatorValue;
      termKey += deliminatorValue;
    } else {
      termValueStrs.add(deliminatorValue);
      termKeyStrs.add(deliminatorValue);
    }
    if (deliminatorKey.length() == 1)
      termKey += deliminatorKey;
    else
      termKeyStrs.add(deliminatorKey);

    termValueInMap = termValue + "}";
    termValueInArray = termValue + "]";
  }
}
