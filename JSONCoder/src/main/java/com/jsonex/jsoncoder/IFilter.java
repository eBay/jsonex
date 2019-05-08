/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder;

public interface IFilter {
  Class<?> getType();

  /**
   * Return null, means uncertain, the caller will continue to next filter. Otherwise, will stop  
   */
  Boolean isFieldSkipped(String field);
}
