/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.jsoncoder;

import org.jsonex.core.charsource.CharSource;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.TDPath;
import org.jsonex.treedoc.json.TDJSONParser;
import org.jsonex.treedoc.json.TDJSONWriter;
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
      TDNode tdNode = req.tdNode;
      if (tdNode == null && req.source != null) {
        tdNode = TDJSONParser.get().parse(req.source, opt.getJsonOption());
      }

      if (req.nodePath != null)
        tdNode = tdNode.getByPath(TDPath.parse(req.nodePath));

      if (tdNode == null)
        return null;

      return (T) BeanCoder.get().decode(tdNode, req.getType(), req.target, "", new BeanCoderContext(opt));
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
  public static <T> T decode(CharSource source, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setSource(source), opt); }
  @SuppressWarnings("unchecked")
  public static <T> T decode(TDNode treeDocNode, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setTdNode(treeDocNode), opt); }

  public <T> T decode(DecodeReq<T> req) { return decode(req, option); }
  public <T> T decodeTo(String str, T target) {
    return decode(DecodeReq.<T>of(target.getClass()).setJson(str).setTarget(target));
  }
  public <T> T decode(String str, Class<T> type) { return decode(str, type, option); }
  @SuppressWarnings("unchecked")
  public <T> T decode(Reader reader, Class<T> type) { return (T)decode(DecodeReq.of(type).setReader(reader), option); }
  public <T> T decode(CharSource source, Class<T> type) { return (T)decode(DecodeReq.of(type).setSource(source), option); }
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
      TDNode jsonNode = BeanCoder.get().encode(req.object, new BeanCoderContext(opt), req.type);
      TDJSONWriter.get().write(writer, jsonNode, opt.getJsonOption());
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
