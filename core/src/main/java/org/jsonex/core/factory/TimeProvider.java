/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package org.jsonex.core.factory;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A time provider to provide abstraction of system time. So that application logic can avoid direct dependency of system time
 * which allows injection of mock time provider for testing
 */
public interface TimeProvider {
  InjectableInstance<TimeProvider> it = InjectableInstance.of(Impl.class);
  static TimeProvider get() { return it.get(); }

  class Impl implements TimeProvider {
    @Override public Date getDate() { return new Date(); }
    @Override public long getTimeMillis() { return System.currentTimeMillis(); }
    @Override public long getNanoTime() { return System.nanoTime(); }
  }

  @Accessors(chain = true)
  class Mock implements TimeProvider {
    @Setter @Getter long nanoTime = System.nanoTime();

    @Override public long getTimeMillis() { return TimeUnit.NANOSECONDS.toMillis(nanoTime); }
    public TimeProvider setTimeMillis(long ms) { nanoTime = TimeUnit.MILLISECONDS.toNanos(ms); return this; }

    @Override public Date getDate() { return new Date(getTimeMillis()); }
    @Deprecated
    public void add(long offset) { nanoTime += TimeUnit.MILLISECONDS.toNanos(offset); }
    public void sleepMs(long offset) { nanoTime += TimeUnit.MILLISECONDS.toNanos(offset); }
  }

  Date getDate();
  long getTimeMillis();
  long getNanoTime();

  default Date now(int offset, TimeUnit unit) {
    return new Date(getTimeMillis() + unit.toMillis(offset));
  }
  default Date now(int offset) {
    return new Date(getTimeMillis() + offset);
  }
  default Date now() { return getDate(); }
}
