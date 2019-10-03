/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc;

import com.jsonex.core.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.jsonex.core.util.StringUtil.*;
import static java.lang.Integer.parseInt;

/** A Node in TreeDoc */
@RequiredArgsConstructor @Getter @Setter @Accessors(chain = true)
@EqualsAndHashCode(exclude = {"parent", "start", "length"}) @ToString(exclude = "parent")
public class TDNode {
  public enum Type { MAP, ARRAY, SIMPLE }
  TDNode parent;
  Type type = Type.SIMPLE;
  /** The key of the node, null for root or array element */
  final String key;
  /** The value of the node, only available for leave node */
  Object value;
  /** Children of node. Use List instead of Map to avoid performance overhead of HashMap for small number of elements */
  List<TDNode> children;
  /** Start position in the source */
  int start;
  /** Length of this node in the source */
  int length;
  /** indicate this node is a deduped Array node for textproto which allows duplicated keys */
  boolean deduped;

  // Create a root node
  public TDNode() { this (null); }

  public TDNode createChild(String name) {
    int childIndex = indexOf(name);
    if (childIndex < 0) {
      TDNode cn = new TDNode(name);
      addChild(cn);
      return cn;
    }

    TDNode existNode = children.get(childIndex);

    // special handling for textproto due to it's bad design that allows duplicated keys
    if (!existNode.isDeduped()) {
      TDNode listNode = new TDNode(name).setParent(this).setDeduped(true).setType(Type.ARRAY);
      this.children.set(childIndex, listNode);
      listNode.addChild(existNode);
      existNode = listNode;
    }

    TDNode cn = new TDNode(null);
    existNode.addChild(cn);
    return cn;
  }

  public TDNode addChild(TDNode node) {
    if (children == null)
      children = new ArrayList<>();
    children.add(node);
    node.parent = this;
    return this;
  }

  public TDNode getChild(String name) {
    int idx = indexOf(name);
    return idx < 0 ? null : children.get(idx);
  }

  int indexOf(String name) {
    if (children == null || name == null)
      return -1;

    for (int i = 0; i < children.size(); i++)
      if (name.equals(children.get(i).getKey()))
        return i;
    return -1;
  }

  public Object getChildValue(String name) {
    TDNode cn = getChild(name);
    return cn == null ? null : cn.getValue();
  }

  public TDNode getChild(int idx) {
    if (children == null || idx >= children.size())
      return null;
    return children.get(idx);
  }

  public TDNode getChildByPath(String path) { return getChildByPath(path.split("/"), 0); }
  public Object getValueByPath(String path) {
    TDNode cn = getChildByPath(path);
    return cn == null ? null : cn.getValue();
  }

  public TDNode getChildByPath(String[] path, int idx) {
    if (idx == path.length)
      return this;

    String pi = path[idx];
    TDNode cn = isDigitOnly(pi) ? getChild(parseInt(pi)) : getChild(pi);
    return cn == null ? null : cn.getChildByPath(path, idx + 1);
  }

  public boolean hasChildren() { return children != null && !children.isEmpty(); }
  public int getChildrenSize() { return children == null ? 0 : children.size(); }

  public boolean isRoot() { return parent == null; }
}
