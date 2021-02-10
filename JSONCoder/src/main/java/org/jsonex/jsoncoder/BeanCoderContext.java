/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsonex.treedoc.TDNode;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Context object for BeanCoder, it will save session information during encoding and provide a
 * way to let client to customize the encode behavior.
 */
@RequiredArgsConstructor
public class BeanCoderContext {
  private int nextId = 1;   // The next id for $ref
  int objectCount = 0;
  @Getter final JSONCoderOption option;

  /**
   * Used only for encoding, remember encoded objects to be used to dedupWithreference
   * Key is the Object to be encoded or EqualsWrapper or the object, the value of this map is the converted result
   */
  @Getter final Map<Object, TDNode> objToNodeMap = new IdentityHashMap<>();

  /**
   * Used only for decoding, remember decoded objects, key is the hash
   */
  @Getter final Map<TDNode, Object> nodeToObjectMap = new HashMap<>();

  /**
   * The current path of the encoding or decoding, mainly used as Deque (Stack), use LinkedList here as we also need random access
   */
  @Getter final LinkedList<Object> objectPath = new LinkedList<>();

  public BeanCoderContext reset() {
    objToNodeMap.clear();
    nodeToObjectMap.clear();
    objectPath.clear();
    return this;
  }

  public int getNextId() { return nextId ++; }

  public TDNode encode(Object obj, Type type, TDNode target) {
    return BeanCoder.get()._encode(obj, this, type, target);
  }

  public Object decode(TDNode jsonNode, Type type, Object targetObj, String name) {
    return BeanCoder.get().decode(jsonNode, type, targetObj, name, this);
  }
}
