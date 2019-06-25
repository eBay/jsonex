/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.jsonex.core.factory.TimeProvider;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class TimeProviderTest {
  @Test public void testTimeProvider() {
    TimeProvider.get().getDate();  // Warm up, so that following test could pass
    assertEquals(new Date(), TimeProvider.get().getDate());
    assertEquals(System.currentTimeMillis(), TimeProvider.get().getTimeMillis());
  }

  @Test public void testMock() {
    TimeProvider.Mock mock = new TimeProvider.Mock();
    TimeProvider.instance.setInstance(mock);
    mock.setTimeMillis(1000);
    assertEquals(1000, TimeProvider.get().getTimeMillis());

    mock.add(1000);
    assertEquals(2000, TimeProvider.get().getTimeMillis());
    assertEquals(2000, TimeProvider.get().getDate().getTime());
    TimeProvider.instance.reset();
  }
}
