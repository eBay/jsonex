/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncoder.treedoc.TDNode;

import java.lang.reflect.Type;

/**
 * Used for specific BeanCoder for certain types, such as Money, Date. This coder will convert between an Object and a TDNode representation
 */
public interface ICoder<T> {
  /**
   * @return The type this Coder is applied to
   */
  Class<T> getType();
   
  /**
   * Encode an Object
   *
   * @param obj  The Object to encode
   * @param context  Encode context
   * @param target  The target json TDNode
   * @return  The target passed as parameter
   */
  TDNode encode(T obj, Type type, BeanCoderContext context, TDNode target);
  
  /**
   * Decode an Object
   *
   * @param jsonNode The json TDNode to be decoded
   * @param type  The target type
   * @param context  Decode context
   * @return The decoded Object
   */
  T decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context);
}
