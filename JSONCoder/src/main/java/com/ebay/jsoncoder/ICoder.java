/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import java.lang.reflect.Type;

/**
 * Used for specific BeanCoder for certain types, such as Money, Date
 * This coder will convert between an Object and a Map representation
 * The Map Representation should only contains Array, List and String
 */
public interface ICoder<T> {
  /**
   * @return The type this Coder is applied to
   */
  Class<T> getType();
   
  /**
   * Encode an Object
   * @param obj  The Object to encode
   * @param context  Encode context
   * @return  Can only be Map, List, String, primitive types or null
   */
  Object encode(T obj, Type type, BeanCoderContext context);
  
  /**
   * Decode an Object
   * @param obj Can only be Map, List, String, primitive types or null. It must matches the return type of encode method
   * @param type  The target type
   * @param context  Decode context
   * @return The decoded Object
   */
  T decode(Object obj, Type type, Object targetObj, BeanCoderContext context);
}
