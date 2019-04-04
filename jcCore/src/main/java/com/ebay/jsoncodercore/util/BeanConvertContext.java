/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import lombok.Getter;
import lombok.Setter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BeanConvertContext {
  /**
   * Used by bean convert to return the conversion errors.
   */
  @Getter @Setter String dateFormat = "yyyy/MM/dd";
  
  /**
   *  If this field is empty, we will include all fields in the bean
   */
  @Getter final Set<String> includedFields = new HashSet<>();
 
  /**
   * If this field is not empty, we will exclude all fields in this array. 
   * This take higher priority than includeFields if the field in both array.
   */
  @Getter final Set<String> excludedFields = new HashSet<>();

  @Getter final Map<String, String> errorValues = new HashMap<>(); //NOPMD
  @Getter final Map<String, Exception> errors = new HashMap<>(); //NOPMD
  
  public String toErrorString() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    for  (Entry<String, String> entry : errorValues.entrySet()) {
      pw.println("\nkey=" + entry.getKey());
      pw.println("value=" + entry.getValue());
      Exception e = errors.get(entry.getKey());
      if (e != null) {
        e.printStackTrace(pw);
      }
    }
    return sw.toString();
  }
  
  public boolean isFieldIncluded(String field) {
    if (excludedFields.contains(field))
      return false;

    return includedFields.isEmpty() || includedFields.contains(field);
  }
}