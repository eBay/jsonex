/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

/**
 * For eBay BO, there is no way to check actual equality of two BO objects. 
 * So this wrapper is to solve this issue. To override the hashCode() and equals()
 * methods.
 * 
 * <p>This class is using Wrapper pattern and Prototype pattern.
 * 
 * <p>It's a prototype, so that client can create a single prototype instance and
 * set to BeanCoderContext.
 * 
 * <p>At encoding runtime, the actually wrapper object will be cloned from the prototype
 * object.
 */
public interface EqualsWrapper<T> {
  /**
   * Get type could return null, to avoid unnecessary dependency. 
   * @return The type this Wrapper will be applied to
   */
  Class<T> getType();
  
  /**
   * Prototype clone method with actually wrapped object
   */
  EqualsWrapper <T> newWrapper(T obj);

  /**
   * @return The wrapped Object
   */
  T getObject();
  
  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();
}
