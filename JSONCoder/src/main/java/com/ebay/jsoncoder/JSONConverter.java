/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncodercore.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Accessors(chain = true)
public class JSONConverter {
  @Accessors(chain = true)
  public static class JSONOption {
    @Getter private int indentFactor;
    @Getter @Setter boolean alwaysQuoteName = true;
    @Getter @Setter char quoteChar = '"';
    @Getter private String indentStr = "";  //Used internally
    public void setIndentFactor(int _indentFactor) {
      this.indentFactor = _indentFactor;
      indentStr = StringUtil.appendRepeatedly(new StringBuilder(), ' ', indentFactor).toString();
    }
  }
  
  /**
   * To JSON String without JSON library, JSON library uses HashMap which
   * causes the out put attributes out of sequence. So another implementation
   * is created
   * 
   * @param out The writer which out put the text
   * @param obj The object to be converted.
   */
  public static void toJSONString(Appendable out, Object obj, JSONOption opt, String indentStr) throws IOException {
    if (obj == null) {
      out.append("null");
      return;
    }

    boolean isCompact = opt.getIndentFactor() == 0;
    String childIndentStr = "";
    if (!isCompact)
      childIndentStr = indentStr + opt.getIndentStr();

    if (obj instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>) obj;
      out.append('{');
      int i = 0;
      for (Entry<?, ?> entry : map.entrySet()) {
        if (!isCompact) {
          out.append('\n');
          out.append(childIndentStr);
        }
        String key = ((String) entry.getKey());
        if (!StringUtil.isJavaIdentifier(key) || opt.alwaysQuoteName)  // Quote the key in case  it's not valid java identifier
          writeQuotedString(out, key, opt.quoteChar);
        else
          out.append(key);
        out.append(":");
        toJSONString(out, entry.getValue(), opt, childIndentStr);
        if (i < map.size() - 1) // No need "," for last entry
          out.append(",");
        i++;
      }

      if (!isCompact && !map.isEmpty()) {
        out.append('\n');
        out.append(indentStr);
      }
      out.append('}');
      return;
    }

    if (obj instanceof List<?>) {
      List<?> list = (List<?>) obj;
      out.append('[');
      for (int i = 0; i < list.size(); i++) {
        Object e = list.get(i);
        if (!isCompact) {
          out.append('\n');
          out.append(childIndentStr);
        }
        toJSONString(out, e, opt, childIndentStr);
        if (i < list.size() - 1) // No need "," for last entry
          out.append(",");
      }

      if (!isCompact && !list.isEmpty()) {
        out.append('\n');
        out.append(indentStr);
      }
      out.append(']');
      return;
    }

    if (obj instanceof String) {
      writeQuotedString(out, (String)obj, opt.quoteChar);
      return;
    }

    if (obj instanceof Character) {
      writeQuotedString(out, String.valueOf(obj), opt.quoteChar);
      return;
    }

    out.append(obj.toString());
  }

  private static void writeQuotedString(Appendable out, String str, char quoteChar) throws IOException {
    out.append(quoteChar);
    out.append(StringUtil.cEscape(str, quoteChar, true));
    out.append(quoteChar);
  }

  /**
   * Convert JSON object into plain map/list representation.
   * 
   * @param json  Can be JSONObject, JSONArray, or null
   * @return Could be Map, List or null
   */
  public static Object jsonObjectToMap(Object json) {
    try {
      if (json == null)
        return null;
      if (json instanceof JSONObject) {
        JSONObject jsonObj = (JSONObject) json;
        Map<String, Object> map = new LinkedHashMap<>();//NOPMD
        for (String key : jsonObj.keySet())
          map.put(key, jsonObjectToMap(jsonObj.get(key)));
        return map;
      }
      if (json instanceof JSONArray) {
        JSONArray jsonArray = (JSONArray) json;
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
          list.add(jsonObjectToMap(jsonArray.get(i)));
        return list;
      } else if (json == JSONObject.NULL) {
        return null;
      } else
        return json;
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }  
}
