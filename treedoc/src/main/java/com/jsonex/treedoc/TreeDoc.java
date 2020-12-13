package com.jsonex.treedoc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true) @RequiredArgsConstructor @Setter @Getter
public class TreeDoc {
  final Map<String, TDNode> idMap = new HashMap<>();
  final URI uri;
  final TDNode root;

  public TreeDoc() { this(null); }

  public TreeDoc(URI uri) {
    this(uri, "root");
  }

  public TreeDoc(URI uri, String rootKey) {
    this (uri, new TDNode((TreeDoc) null, rootKey));
    this.root.doc = this;
  }

  public static TreeDoc ofArray() {
    TreeDoc result = new TreeDoc();
    result.root.setType(TDNode.Type.ARRAY);
    return result;
  }

  /**
   * Create a TreeDoc with array root node contains the input nodes. This method will mutate the input nodes without
   * copying them. So the original Treedoc and parent associated with nodes will be obsoleted.
   * For idMap merge, if there's duplicated keys, later one will override previous one.
   */
  public static TreeDoc ofNodes(Collection<TDNode> nodes) {
    TreeDoc result = new TreeDoc();
    result.root.type = TDNode.Type.ARRAY;
    for (TDNode node : nodes) {
      node.setKey(null);
      result.idMap.putAll(node.doc.idMap);
      result.root.addChild(node);
    }
    return result;
  }

  /**
   * Build a tree node with exiting node as root node. This method will mutate input node so that the original doc and
   * parent associated with that node will be obsoleted. The that node is still associated with original doc, the original
   * doc will be in invalid state.
   */
  public static TreeDoc ofNode(TDNode node) {
    String key = node.getDoc().getRoot().getKey();
    TreeDoc result = new TreeDoc(node.doc.uri, node.setKey(key));
    result.idMap.putAll(node.doc.idMap);
    node.doc = result;
    node.parent = null;
    return result;
  }
}
