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

public class BeanConvertContext {
  /**
   * Used by bean convert to return the conversion errors.
   */
  @Getter @Setter String dateFormat = "yyyy/MM/dd";
}