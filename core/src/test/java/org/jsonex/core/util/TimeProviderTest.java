/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.util;

import org.jsonex.core.factory.TimeProvider;
import org.junit.After;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

public class TimeProviderTest {
  @Test public void testTimeProvider() {
    TimeProvider.get().getDate();  // Warm up, so that following test could pass
    // Flaky testing only if CPU is super slow
    assertEquals(new Date(), TimeProvider.get().getDate());
    assertEquals(System.currentTimeMillis(), TimeProvider.get().getTimeMillis());
    assertEquals(System.currentTimeMillis() + 1000, TimeProvider.get().now(1, TimeUnit.SECONDS).getTime());
    assertEquals(System.currentTimeMillis() - 2000, TimeProvider.get().now(-2000).getTime());
  }

  @Test public void testMock() {
    TimeProvider.Mock mock = new TimeProvider.Mock();
    TimeProvider.it.setInstance(mock);
    mock.setTimeMillis(1000);
    assertEquals(1000, TimeProvider.get().getTimeMillis());

    mock.sleepMs(1000);
    assertEquals(2000, TimeProvider.get().getTimeMillis());
    assertEquals(2000, TimeProvider.get().getDate().getTime());
  }

  @After public void after() {
    TimeProvider.it.reset();
  }
}
