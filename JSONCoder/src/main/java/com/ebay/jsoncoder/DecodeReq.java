/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Decode Request, the reason we use abstract class, is to force create a sub-class so that it's possible to get
 * the getActualTypeArguments
 */
@SuppressWarnings("WeakerAccess")
@Accessors(chain=true)
public abstract class DecodeReq<T> {
  @Setter private Type type;
  @Getter @Setter Reader reader;
  /**
   * Could be JSONObject, JSONArray, or null
   */
  @Getter @Setter Object json;
  @Getter @Setter T target;
  
  public DecodeReq(Reader reader) { this.reader = reader; }
  public DecodeReq(JSONObject json) { this.json = json; }
  public DecodeReq(JSONArray json) { this.json = json; }
  public DecodeReq(String jsonStr) { this.setJsonString(jsonStr); }
  
  public DecodeReq<T> setJsonString(String jsonStr) {
    reader = jsonStr == null ? null : new StringReader(jsonStr);
    return this;
  }
  
  public Type getType() {
    if (type == null) {
      Type superClass = getClass().getGenericSuperclass();
      type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
    return type;
  }
  
  public DecodeReq(Type type) { this.type = type;}
  public static <T> DecodeReq<T> of(Type type) { return new DecodeReq<T>(type){}; }
}
