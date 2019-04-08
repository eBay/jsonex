/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import java.lang.reflect.Type;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.BeanCoderException;
import com.ebay.jsoncoder.ICoder;
import com.ebay.jsoncoder.treedoc.TDNode;

public class CoderXMLGregorianCalendar implements ICoder<XMLGregorianCalendar>{
  @Override public Class<XMLGregorianCalendar> getType() {return XMLGregorianCalendar.class;}
  @Override public TDNode encode(XMLGregorianCalendar obj, Type type, BeanCoderContext ctx, TDNode target) {
    String str = ctx.getCachedDateFormat(ctx.getOption().getDateFormat()).format(obj.toGregorianCalendar().getTime());
    return target.setValue(str);
  }

  public XMLGregorianCalendar decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context) {
    try{
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTime(CoderDate._decode(jsonNode.getValue(), context));
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    }catch(Exception e){
      throw new BeanCoderException(e);
    }
  }
}