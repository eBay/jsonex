/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import lombok.Getter;

/**
 * This generic abstract class is used for obtaining full generic type information
 * by sub-classing;  
 */
public abstract class TypeRef<T> {
  @Getter final Type type;
  protected TypeRef(){
    Type superClass = getClass().getGenericSuperclass();
    type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
  }

  public final static Type LIST_OF_STRING = new TypeRef<List<String>>() {}.type;
  public final static Type LIST_OF_INTEGER = new TypeRef<List<Integer>>() {}.type;
  public final static Type LIST_OF_LONG = new TypeRef<List<Long>>() {}.type;
  
  public final static Type SET_OF_STRING = new TypeRef<Set<String>>() {}.type;
  public final static Type SET_OF_INTEGER = new TypeRef<Set<Integer>>() {}.type;
  public final static Type SET_OF_LONG = new TypeRef<Set<Long>>() {}.type; 
}
