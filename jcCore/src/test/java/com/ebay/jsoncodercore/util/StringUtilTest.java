/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class StringUtilTest {
  @Test public void testCEscape() {
    String org1 = "a\"\n\t";
    String dest = "a\\\"\\n\\t";
    assertEquals(dest, StringUtil.cEscape(org1));
    String org2 = "'test'";
    String des2 = "\\'test\\'";
    assertEquals(des2, StringUtil.cEscape(org2, '\'', false));
    assertEquals(org2, StringUtil.cEscape(org2));
  }
}
