/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncoder.treedoc.TDNode;
import com.ebay.jsoncodercore.factory.InjectableFactory;
import com.ebay.jsoncodercore.factory.InjectableFactory.CachePolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Context object for BeanCoder, it will save session information during encoding and provide a
 * way to let client to customize the encode behavior.
 */
@RequiredArgsConstructor
public class BeanCoderContext {
  // For performance reason, we need to cache SimpleDateFormat in the same thread as SimpleDateFormat is not threadsafe
  public static final InjectableFactory<String, SimpleDateFormat> dateFormatCache =
      InjectableFactory.of(String.class, SimpleDateFormat.class, CachePolicy.THREAD_LOCAL);

  @Getter final JSONCoderOption option;

  /**
   * Used only for encoding, remember encoded objects to be used to dedupWithreference
   * Key is the Object to be encoded or EqualsWrapper or the object, the value of this map is the converted result
   */
  @Getter final Map<Object, TDNode> convertedObjects = new HashMap<>();

  /**
   * Used only for decoding, remember decoded objects, key is the hash
   */
  @Getter final Map<String, Object> hashToObjectMap = new HashMap<>();

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

  public TDNode encode(Object obj, Type type, TDNode target) { return BeanCoder._encode(obj, this, type, target); }

  public Object decode(TDNode jsonNode, Type type, Object targetObj, String name) {
    return BeanCoder.decode(jsonNode, type, targetObj, name, this);
  }

  public SimpleDateFormat getCachedDateFormat(String dateFmt) { return dateFormatCache.get(dateFmt); }
}
