/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

import org.jsonex.core.charsource.ArrayCharSource;
import org.jsonex.core.charsource.CharSource;
import org.jsonex.core.charsource.ReaderCharSource;
import org.jsonex.treedoc.TDNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Decode Request, the reason we use abstract class, is to force to create a sub-class so that it's possible to get
 * the getActualTypeArguments. If generic type is not of the concern, use factory method "of".
 *
 * <p> It can be used to specify source of the JSON document, either through a String, Reader, or a a wrapped CharSource. As
 *
 */
@SuppressWarnings("WeakerAccess")
@Accessors(chain=true)
public abstract class DecodeReq<T> {
  /** The target type */
  @Setter private Type type;

  /** The source of the JSON string */
  @Getter @Setter CharSource source;

  /** The TreeDoc node, if it's provided, this attribute will override source attribute */
  @Getter @Setter TDNode tdNode;

  /** Optional node path, if it's provided, it will decode the children node with the specified path */
  @Getter @Setter String nodePath;

  /** Optional target Object, if it's provide, it will incremental decode to the target object */
  @Getter @Setter T target;

  /** Set source with a reader */
  public DecodeReq<T> setReader(Reader reader) {
    source = reader == null ? null : new ReaderCharSource(reader);
    return this;
  }

  /** Set source of a json string */
  public DecodeReq<T> setJson(String jsonStr) {
    source = jsonStr == null ? null : new ArrayCharSource(jsonStr);
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
  public DecodeReq() { }
  public static <T> DecodeReq<T> of(Type type) { return new DecodeReq<T>(type){}; }
  public static <T> DecodeReq<T> of(Class<T> cls) { return new DecodeReq<T>(cls){}; }
}
