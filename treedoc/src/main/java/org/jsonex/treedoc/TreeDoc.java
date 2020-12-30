package org.jsonex.treedoc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true) @Setter @Getter @RequiredArgsConstructor
public class TreeDoc {
  final Map<String, TDNode> idMap = new HashMap<>();
  final URI uri;
  TDNode root = new TDNode(this, "root");

  public TreeDoc() { this(null); }

  public TreeDoc(URI uri, String rootKey) {
    this (uri);
    this.root.key = rootKey;
  }

  public static TreeDoc ofArray() {
    TreeDoc result = new TreeDoc();
    result.root.setType(TDNode.Type.ARRAY);
    return result;
  }

  /** Retrain only the sub-tree under the input node. */
  public TreeDoc retain(TDNode node) {
    node.setKey(this.root.getKey());
    this.root = node;
    node.parent = null;
    return this;
  }

  /**
   * Create a TreeDoc with array root node contains the input nodes. This method will mutate the input nodes without
   * copying them. So the original Treedoc and parent associated with nodes will be obsoleted.
   * For idMap merge, if there's duplicated keys, later one will override previous one.
   */
  public static TreeDoc merge(Collection<TDNode> nodes) {
    TreeDoc result = new TreeDoc();
    result.root.type = TDNode.Type.ARRAY;
    for (TDNode node : nodes) {
      node.setKey(null);
      result.idMap.putAll(node.doc.idMap);
      result.root.addChild(node);
    }
    result.root.foreach(n -> n.doc = result);
    return result;
  }
}
