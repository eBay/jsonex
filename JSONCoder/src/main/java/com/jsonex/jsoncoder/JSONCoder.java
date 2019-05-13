/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

import com.jsonex.treedoc.TDJSONParser;
import com.jsonex.treedoc.TDJSONWriter;
import com.jsonex.treedoc.TDNode;
import lombok.Getter;

import java.io.Reader;
import java.io.Writer;

@SuppressWarnings("ALL")
public class JSONCoder {
  @Getter static final JSONCoder global = new JSONCoder(JSONCoderOption.global);

  @Getter final JSONCoderOption option;
  public JSONCoder(JSONCoderOption option) { this.option = option; }
  
  @SuppressWarnings("unchecked")
  public static <T> T decode(DecodeReq<T> req, JSONCoderOption opt) {
    try {
      TDNode jsonNode = req.tdNode;
      if (jsonNode == null && req.source != null) {
        jsonNode = TDJSONParser.getInstance().parse(req.source);
      }

      if (req.nodePath != null)
        jsonNode = jsonNode.getChildByPath(req.nodePath);
      
      if (jsonNode == null)
        return null;

      return (T) BeanCoder.decode(jsonNode, req.getType(), req.target, "", new BeanCoderContext(opt));
    } catch (Exception e) {
      if (e instanceof BeanCoderException)
        throw (BeanCoderException)e;
      throw new BeanCoderException(e);
    }
  }
  
  @SuppressWarnings({"unchecked", "WeakerAccess"})
  public static <T> T decode(String jsonStr, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setJson(jsonStr), opt); }
  @SuppressWarnings("unchecked")
  public static <T> T decode(Reader reader, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setReader(reader), opt); }
  @SuppressWarnings("unchecked")
  public static <T> T decode(TDNode treeDocNode, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setTdNode(treeDocNode), opt); }

  public <T> T decode(DecodeReq<T> req) { return decode(req, option); }
  public <T> T decodeTo(String str, T target) {
    return decode(DecodeReq.<T>of(target.getClass()).setJson(str).setTarget(target));
  }
  public <T> T decode(String str, Class<T> type) { return decode(str, type, option); }
  @SuppressWarnings("unchecked")
  public <T> T decode(Reader reader, Class<T> type) { return (T)decode(DecodeReq.of(type).setReader(reader), option); }
  @SuppressWarnings("unchecked")
  public <T> T decode(TDNode treeDocNode, Class<T> type) { return (T)decode(DecodeReq.of(type).setTdNode(treeDocNode), option); }


  /**
   * @param req
   * @param opt
   * @return
   */
  public static String encode(EncodeReq req, JSONCoderOption opt) {
    try {
      StringBuilder sWriter = null;
      Appendable writer = req.writer;
      if (writer == null) {
        writer = sWriter = new StringBuilder();
      }
      TDNode jsonNode = BeanCoder.encode(req.object, new BeanCoderContext(opt), req.type);
      TDJSONWriter.getInstance().write(writer, jsonNode, opt.getJsonOption());
      return sWriter == null ? null : sWriter.toString();
    } catch (Exception e) {
      throw new BeanCoderException(e);
    }
  }
  public static String encode(Object obj, JSONCoderOption opt) { return encode(EncodeReq.of(obj), opt); }
  public static void encode(Object obj, Writer writer, JSONCoderOption opt) { encode(EncodeReq.of(obj).setWriter(writer), opt); }

  public String encode(EncodeReq req) { return encode(req, option); }
  public String encode(Object obj) { return encode(obj, option); }
  public void encode(Object obj, Writer writer) { encode(obj, writer, option); }
}
