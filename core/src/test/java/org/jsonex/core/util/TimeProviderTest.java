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
    TimeProvider.get().getNanoTime();  // Warm up Clock Instance as the class loader will take 20ms which will break test
    // Flaky testing only if CPU is super slow
    assertEquals(new Date(), TimeProvider.get().getDate());
    assertEquals(System.currentTimeMillis(), TimeProvider.get().getTimeMillis());
    assertEquals(System.currentTimeMillis(), TimeProvider.get().getNanoTime() / 1_000_000);
    assertEquals(System.currentTimeMillis() + 1000, TimeProvider.now(1, TimeUnit.SECONDS));
    assertEquals(System.currentTimeMillis() - 2000, TimeProvider.now(-2000));
    assertEquals(TimeProvider.get().getTimeMillis(), TimeProvider.millis());
    // assertEquals(TimeProvider.get().getNanoTime(), TimeProvider.nano());  // Won't equals
    assertEquals(TimeProvider.get().getTimeMillis() - 1000, TimeProvider.duration(1000));
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
