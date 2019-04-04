/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.BeanCoderException;
import com.ebay.jsoncoder.JSONCoderOption;
import com.ebay.jsoncoder.ICoder;
import com.ebay.jsoncodercore.factory.InjectableFactory;
import com.ebay.jsoncodercore.factory.InjectableFactory.CachePolicy;

public class CoderDate implements ICoder<Date> {
  // For performance reason, we need to cache SimpleDateFormat in the same thread
  private final static InjectableFactory<String, SimpleDateFormat> dateFormatCache =
      InjectableFactory.of(String.class, SimpleDateFormat.class, CachePolicy.THREAD_LOCAL);
  public Class<Date> getType() {return Date.class;}
  
  @Override
  public Object encode(Date obj, Type type, BeanCoderContext context) {
    String dateFormat = context.getOption().getDateFormat();
    if (dateFormat == null)
      return obj.getTime();
    return dateFormatCache.get(dateFormat).format(obj);
  }

  @Override
  public Date decode(Object obj, Type type, Object targetObj, BeanCoderContext context) { return _decode(obj, context); }
  
  static Date _decode(Object obj, BeanCoderContext context) {
    if (obj instanceof Number)
      return new Date(((Number) obj).longValue());

    JSONCoderOption opt = context.getOption();
    String dateStr = (String) obj;
    try {
      return dateFormatCache.get(opt.getDateFormat()).parse(dateStr);
    } catch(ParseException e) {
      try { 
        return opt.parseDateFullback(dateStr);
      } catch (ParseException e1) {
        throw new BeanCoderException(e1);
      }
    }
  }
}