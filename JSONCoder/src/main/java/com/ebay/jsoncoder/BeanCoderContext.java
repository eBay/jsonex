/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Context object for BeanCoder, it will save session information during encoding and provide a
 * way to let client to customize the encode behavior.
 */
@RequiredArgsConstructor
public class BeanCoderContext {
  @Getter final JSONCoderOption option;

  /**
   * Used only for encoding, remember encoded objects to be used to dedupWithreference
   * Key is the Object to be encoded or EqualsWrapper or the object, the value of this map is the converted result
   */
  @Getter final Map<Object, Map<String, Object>> convertedObjects = new HashMap<>();//NOPMD

  /**
   * Used only for decoding, remember decoded objects, key is the hash
   */
  @Getter final Map<String, Object> hashToObjectMap = new HashMap<>();//NOPMD

  /**
   * The current path of the encoding or decoding, mainly used as Deque (Stack), use LinkedList here as we also need random access
   */
  @Getter final LinkedList<Object> objectPath = new LinkedList<>();

  public BeanCoderContext reset() {
    convertedObjects.clear();
    hashToObjectMap.clear();
    objectPath.clear();
    return this;
  }

  public Object encode(Object obj, Type type) { return BeanCoder._encode(obj, this, type); }

  public Object decode(Object obj, Type type, Object targetObj, String name) {
    return BeanCoder.decode(obj, type, targetObj, name, this);
  }
  
}
