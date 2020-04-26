/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc;

import com.jsonex.treedoc.TDPath.Part;
import java.util.Objects;
import javax.swing.tree.TreeNode;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/** A Node in TreeDoc */
@RequiredArgsConstructor
// @Getter @Setter
@Accessors(chain = true)
 @EqualsAndHashCode(exclude = {"parent", "start", "end", "doc"}) @ToString(exclude = {"parent", "doc"})
public class TDNode {
  public enum Type { MAP, ARRAY, SIMPLE }
  @Getter final TreeDoc doc;
  @Getter TDNode parent;
  @Getter @Setter Type type = Type.SIMPLE;
  /** The key of the node, null for root or array element */
  @Getter String key;
  /** The value of the node, only available for leave node */
  @Getter Object value;
  /** Children of node. Use List instead of Map to avoid performance overhead of HashMap for small number of elements */
  @Getter List<TDNode> children;
  /** Start position in the source */
  @Getter @Setter Bookmark start;
  /** Length of this node in the source */
  @Getter @Setter Bookmark end;
  /** indicate this node is a deduped Array node for textproto which allows duplicated keys */
  transient private boolean deduped;
  transient private int hash;
  transient private String str;

  public TDNode(TDNode parent, String key) {
    this(parent.doc);
    this.parent = parent;
    this.key = key;
  }

  public TDNode(TreeDoc doc, String key) {
    this(doc);
    this.key = key;
  }

  public TDNode setKey(String key) {
    this.key = key;
    return touch();
  }

  public TDNode setValue(Object value) {
    this.value = value;
    return touch();
  }

  // Create a root node
  public TDNode createChild() { return createChild(null); }
  public TDNode createChild(String name) {
    if (name == null)  // Assume it's array element
      name = "" + getChildrenSize();

    int childIndex = indexOf(name);
    if (childIndex < 0) {
      TDNode cn = new TDNode(doc, name);
      addChild(cn);
      return cn;
    }

    TDNode existNode = children.get(childIndex);

    // special handling for textproto due to it's bad design that allows duplicated keys
    if (!existNode.deduped) {
      TDNode listNode = new TDNode(this, name).setType(Type.ARRAY);
      listNode.deduped = true;
      this.children.set(childIndex, listNode);
      existNode.key = "0";
      listNode.addChild(existNode);
      listNode.start = existNode.start;  // Reuse first node's start and length
      listNode.end = existNode.end;
      existNode = listNode;
    }

    return existNode.createChild();
  }

  public TDNode addChild(TDNode node) {
    if (children == null)
      children = new ArrayList<>();
    children.add(node);
    node.parent = this;
    return touch();
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
    return hasChildren() ? children.get(idx) : null;
  }

  public boolean hasChildren() { return children != null && !children.isEmpty(); }
  public int getChildrenSize() { return children == null ? 0 : children.size(); }

  public Object getValueByPath(String path) { return getValueByPath(TDPath.parse(path)); }
  public Object getValueByPath(TDPath path) {
    TDNode cn = getByPath(path);
    return cn == null ? null : cn.getValue();
  }

  public TDNode getByPath(String path) { return getByPath(TDPath.parse(path)); }
  public TDNode getByPath(String[] path) { return getByPath(TDPath.parse(path)); }
  public TDNode getByPath(TDPath path) { return getByPath(path, false); }

  /** If noNull is true, it will return the last matched node */
  public TDNode getByPath(TDPath path, boolean noNull) { return getByPath(path, 0, noNull); }
  public TDNode getByPath(TDPath path, int idx, boolean noNull) {
    if (idx == path.parts.size())
      return this;

    TDNode next = getNextNode(path.getParts().get(idx));
    if (next == null)
      return noNull ? this : null;

    return next.getByPath(path, idx + 1, noNull);
  }

  private TDNode getNextNode(Part part) {
    switch (part.type) {
      case ROOT: return doc.root;
      case ID: return doc.idMap.get(part.key);
      case RELATIVE: return getAncestor(part.level);
      case CHILD: return getChild(part.key);
      default: return null;  // Impossible
    }
  }

  public TDNode getAncestor(int level) {
    TDNode result = this;
    for (int i = 0; i < level && result != null; i++, result = result.parent)
      ;
    return result;
  }

  public boolean isRoot() { return parent == null; }

  public List<String> getPath() {
    if (parent == null)
      return new ArrayList<>();
    List<String> result = parent.getPath();
    result.add(key);
    return result;
  }

  public boolean isLeaf() { return getChildrenSize() == 0; }

  private TDNode touch() {
    this.hash = 0;
    this.str = null;
    if (parent != null)
      parent.touch();
    return this;
  }

  @Override public String toString() {
    if (str == null)
      str = new StringBuilder(key + "").append(':').append(valueToString(100000)).toString();
    return str;
  }

  private String valueToString(int limit) {
    StringBuilder sb = new StringBuilder();
    if (value != null)
      sb.append(value);

    if (this.children == null)
      return sb.toString();

    sb.append(type == Type.ARRAY ? '[' : '{');
    for (TDNode n : this.children) {
      if (type == Type.MAP)
        sb.append(n.key).append(':');
      sb.append(n.valueToString(limit)).append(',');
      if (sb.length() > limit) {
        sb.append("...");
        break;
      }
    }
    sb.append(type == Type.ARRAY ? ']' : '}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    TDNode tdNode = (TDNode) o;
    return Objects.equals(key, tdNode.key) && Objects.equals(value, tdNode.value) && Objects.equals(children, tdNode.children);
  }

  @Override public int hashCode() {
    if (hash == 0)
      hash = Objects.hash(key, value, children);
    return hash;
  }
}
