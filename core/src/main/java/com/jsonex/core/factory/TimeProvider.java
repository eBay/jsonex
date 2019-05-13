/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.factory;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * A time provider to provide abstraction of system time. So that application logic can avoid direct dependency of system time
 * which allows injection of mock time provider for testing
 */
public interface TimeProvider {
  InjectableInstance<TimeProvider> instance = InjectableInstance.of(Impl.class);
  static TimeProvider getInstance() { return instance.get(); }

  class Impl implements TimeProvider {
    @Override public Date getDate() { return new Date(); }
    @Override public long getTimeMillis() { return System.currentTimeMillis(); }
  }

  class Mock implements TimeProvider {
    @Setter @Getter long timeMillis = System.currentTimeMillis();
    @Override public Date getDate() { return new Date(timeMillis); }
    public void add(long offset) { timeMillis += offset; }
  }

  Date getDate();
  long getTimeMillis();
}
