/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.factory;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public abstract class TimeProvider {
  static final InjectableInstance<TimeProvider> instance = InjectableInstance.of(Impl.class);
  public static TimeProvider getInstance() { return instance.get(); }

  class Impl extends TimeProvider {
    @Override public Date getDate() { return new Date(); }
    @Override public long getTimeMillis() { return System.currentTimeMillis(); }
  }

  class Mock extends TimeProvider {
    @Setter @Getter long timeMillis = System.currentTimeMillis();
    @Override public Date getDate() { return new Date(timeMillis); }
    public void add(long offset) { timeMillis += offset; }
  }

  abstract Date getDate();
  abstract long getTimeMillis();
}
