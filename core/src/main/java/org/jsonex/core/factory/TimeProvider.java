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
  }

  @Accessors(chain = true)
  class Mock implements TimeProvider {
    @Setter @Getter long timeMillis = System.currentTimeMillis();
    @Override public Date getDate() { return new Date(timeMillis); }
    public void add(long offset) { timeMillis += offset; }
  }

  Date getDate();
  long getTimeMillis();
}
