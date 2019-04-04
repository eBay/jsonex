/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.ebay.jsoncoder.BeanCoder;
import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.BeanCoderException;
import com.ebay.jsoncoder.DecodeReq;
import com.ebay.jsoncoder.EncodeReq;
import com.ebay.jsoncoder.JSONCoderOption;
import com.ebay.jsoncoder.JSONConverter;
import com.ebay.jsoncoder.JSONTokenerExt;
import org.json.JSONException;

import lombok.Getter;

@SuppressWarnings("ALL")
public class JSONCoder {
  @Getter static final JSONCoder global = new JSONCoder(JSONCoderOption.global);

  @Getter final JSONCoderOption option;
  public JSONCoder(JSONCoderOption option) { this.option = option; }
  
  @SuppressWarnings("unchecked")
  public static <T> T decode(DecodeReq<T> req, JSONCoderOption opt) {
    try {
      Object json = req.json;
      if (json == null && req.reader != null) {
        JSONTokenerExt jsonTokener = new JSONTokenerExt(req.reader);
        json = jsonTokener.nextValue(true);
      }
      
      if (json == null)
        return null;

      Object map = JSONConverter.jsonObjectToMap(json);
      return (T) BeanCoder.decode(map, req.getType(), req.target, "", new BeanCoderContext(opt));
    } catch (Exception e) {
      if (e instanceof BeanCoderException)
        throw (BeanCoderException)e;
      throw new BeanCoderException(e);
    }
  }
  
  @SuppressWarnings({"unchecked", "WeakerAccess"})
  public static <T> T decode(String jsonStr, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setJsonString(jsonStr), opt); }
  @SuppressWarnings("unchecked")
  public static <T> T decode(Reader reader, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setReader(reader), opt); }
  @SuppressWarnings("unchecked")
  public static <T> T decode(Object json, Class<T> type, JSONCoderOption opt) { return (T)decode(DecodeReq.of(type).setJson(json), opt); }
  
  public static String encode(EncodeReq req, JSONCoderOption opt) {
    try {
      StringBuilder sWriter = null;
      Appendable writer = req.writer;
      if (writer == null) {
        writer = sWriter = new StringBuilder();
      }
      Object map = BeanCoder.encode(req.object, new BeanCoderContext(opt), req.type);
      JSONConverter.toJSONString(writer, map, opt.getJsonOption(), "");
      return sWriter == null ? null : sWriter.toString();
    } catch (IOException e) {
      throw new BeanCoderException(e);
    }
  }
  public static String encode(Object obj, JSONCoderOption opt) { return encode(EncodeReq.of(obj), opt); }
  public static void encode(Object obj, Writer writer, JSONCoderOption opt) { encode(EncodeReq.of(obj).setWriter(writer), opt); }

  public <T> T decode(DecodeReq<T> req) { return decode(req, option); }
  public <T> T decodeTo(String str, T target) {
    return decode(DecodeReq.<T>of(target.getClass()).setJsonString(str).setTarget(target));
  }
  public <T> T decode(String str, Class<T> type) { return decode(str, type, option); }
  @SuppressWarnings("unchecked")
  public <T> T decode(Reader reader, Class<T> type) { return (T)decode(DecodeReq.of(type).setReader(reader), option); }
  @SuppressWarnings("unchecked")
  public <T> T decode(Object json, Class<T> type) { return (T)decode(DecodeReq.of(type).setJson(json), option); }
  

  public String encode(EncodeReq req) { return encode(req, option); }
  public String encode(Object obj) { return encode(obj, option); }
  public void encode(Object obj, Writer writer) { encode(obj, writer, option); }
}
