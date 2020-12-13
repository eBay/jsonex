/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.treedoc;

import com.jsonex.core.charsource.Bookmark;
import com.jsonex.core.type.Lazy;
import com.jsonex.core.util.ListUtil;
import com.jsonex.core.util.StringUtil;
import com.jsonex.treedoc.TDPath.Part;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.jsonex.core.util.ListUtil.last;

/** A Node in TreeDoc */
@RequiredArgsConstructor
// @Getter @Setter
@Accessors(chain = true)
public class TDNode {
  public enum Type { MAP, ARRAY, SIMPLE }
  @Getter TreeDoc doc;
  @Getter @Setter TDNode parent;
  @Getter @Setter Type type = Type.SIMPLE;
  /** The key of the node, null for root */
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
  transient private final Lazy<Integer> hash = new Lazy<>();
  transient private final Lazy<String> str = new Lazy<>();

  public TDNode(TDNode parent, String key) { this.doc = parent.doc; this.parent = parent; this.key = key; }
  public TDNode(TreeDoc doc, String key) { this.doc = doc; this.key = key; }

  public TDNode setKey(String key) {  this.key = key; return touch(); }
  public TDNode setValue(Object value) { this.value = value; return touch(); }

  // Create a child node for array
  public TDNode createChild() { return createChild(null); }
  public TDNode createChild(String name) {
    int childIndex = indexOf(name);
    if (childIndex < 0) {
      TDNode cn = new TDNode(doc, name);
      addChild(cn);
      return cn;
    }

    // special handling for textproto due to it's bad design that allows duplicated keys
    TDNode existNode = children.get(childIndex);
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
    node.parent = this;
    node.doc = doc;
    if (node.key == null)  // Assume it's array element
      node.key = "" + getChildrenSize();
    children.add(node);
    return touch();
  }

  public void swapWith(TDNode to) {
    if (this.parent == null || to.parent == null)
      throw new IllegalArgumentException("Can't swap root node");
    int idx1 = index();
    int idx2 = to.index();
    if (idx1 < 0 || idx2 < 0)
      throw new IllegalArgumentException("Note is not attached to it's parent:idx1=" + idx1 + "; idx2=" + idx2);

    TDNode toParent = to.parent;
    String toKey = to.key;

    parent.children.set(idx1, to);
    to.parent = parent;
    to.key = key;

    toParent.children.set(idx2, this);
    parent = toParent;
    key = toKey;
  }

  public TDNode getChild(String name) {
    int idx = indexOf(name);
    return idx < 0 ? null : children.get(idx);
  }

  int indexOf(TDNode node) { return ListUtil.indexOf(children, n -> n == node); }
  int indexOf(String name) { return ListUtil.indexOf(children, n -> n.getKey().equals(name)); }
  int index() { return parent == null ? 0 : parent.indexOf(this); }

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

  TDNode getNextNode(Part part) {
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

  public String getPathAsString() { return "/" + StringUtil.join(getPath(), "/"); }
  public List<String> getPath() {
    if (parent == null)
      return new ArrayList<>();
    List<String> result = parent.getPath();
    result.add(key);
    return result;
  }

  public boolean isLeaf() { return getChildrenSize() == 0; }

  private TDNode touch() {
    hash.clear();;
    str.clear();;
    if (parent != null)
      parent.touch();
    return this;
  }

  @Override public String toString() {
    return str.getOrCompute(() -> toString(new StringBuilder(), true, true, 100000).toString());
  }

  public StringBuilder toString(StringBuilder sb, boolean includeRootKey, boolean includeReservedKeys, int limit) {
    if (parent != null && parent.type == Type.MAP && includeRootKey)
      sb.append(key + ": ");

    if (value != null) {
      if (!(value instanceof String)) {
        sb.append(value);
      } else {
        String str = StringUtil.cEscape((String) value, '\'');
        int remainLen = limit - sb.length();
        if (str.length() > remainLen)
          str = str.substring(0, remainLen) + "...";
        sb.append('\'' + str + '\'');
      }
    }

    if (this.children == null)
      return sb;

    sb.append(type == Type.ARRAY ? '[' : '{');
    for (TDNode n : this.children) {
      if (!includeReservedKeys && n.key != null && n.key.startsWith("$"))
        continue;
      if (sb.length() > limit) {
        sb.append("...");
        break;
      }
      n.toString(sb, true, includeReservedKeys, limit);
      if (n != last(this.children).get())
        sb.append(", ");
    }
    sb.append(type == Type.ARRAY ? ']' : '}');
    return sb;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    TDNode tdNode = (TDNode) o;
    return Objects.equals(key, tdNode.key) && Objects.equals(value, tdNode.value) && Objects.equals(children, tdNode.children);
  }

  @Override public int hashCode() {
    return hash.getOrCompute(() -> Objects.hash(key, value, children));
  }
}
