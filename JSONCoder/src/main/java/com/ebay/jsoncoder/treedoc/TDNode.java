/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.treedoc;

import com.ebay.jsoncodercore.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/** A Node in TreeDoc */
@RequiredArgsConstructor @Getter @Setter @Accessors(chain = true)
@EqualsAndHashCode(exclude = {"parent", "start", "length"}) @ToString(exclude = "parent")
public class TDNode {
  public enum Type {MAP, ARRAY, SIMPLE}
  final TDNode parent;
  Type type = Type.SIMPLE;
  /** The key of the node, null for root or array element */
  final String key;
  /** The value of the node, only available for leave node */
  Object val;
  /** Children of node. Use List instead of Map to avoid performance overhead of HashMap for small number of elements */
  List<TDNode> children;
  /** Start position in the source */
  int start;
  /** Length of this node in the source */
  int length;

  public TDNode addChild(TDNode node) {
    if (children == null)
      children = new ArrayList<>();
    children.add(node);
    return this;
  }

  public TDNode getChild(String name) {
    if (children == null)
      return null;
    for (TDNode cn : children) {
      if (name.equals(cn.getKey()))
        return cn;
    }
    return null;
  }

  public TDNode getChild(int idx) {
    if (children == null || idx >= children.size())
      return null;
    return children.get(idx);
  }

  public TDNode getChildByPath(String path) { return getChildByPath(path.split("/"), 0); }

  public TDNode getChildByPath(String[] path, int idx) {
    if (idx == path.length)
      return this;

    TDNode cn = StringUtil.isDigitOnly(path[idx]) ? getChild(Integer.parseInt(path[idx])) : getChild(path[idx]);
    return cn == null ? null : cn.getChildByPath(path, idx + 1);
  }
}
