/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import com.ebay.jsoncodercore.factory.TimeProvider;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class TimeProviderTest {
  @Test public void testTimeProvider() {
    TimeProvider.getInstance().getDate();  // Warm up, so that following test could pass
    assertEquals(new Date(), TimeProvider.getInstance().getDate());
    assertEquals(System.currentTimeMillis(), TimeProvider.getInstance().getTimeMillis());
  }

  @Test public void testMock() {
    TimeProvider.Mock mock = new TimeProvider.Mock();
    TimeProvider.instance.setInstance(mock);
    mock.setTimeMillis(1000);
    assertEquals(1000, TimeProvider.getInstance().getTimeMillis());

    mock.add(1000);
    assertEquals(2000, TimeProvider.getInstance().getTimeMillis());
    assertEquals(2000, TimeProvider.getInstance().getDate().getTime());
    TimeProvider.instance.reset();
  }
}
