/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.BeanCoderException;
import com.ebay.jsoncoder.ICoder;

public class CoderXMLGregorianCalendar implements ICoder<XMLGregorianCalendar>{
  //private final static String df = "yyyy/MM/dd HH:mm:ss.SSS.Z";
  
  public Class<XMLGregorianCalendar> getType() {return XMLGregorianCalendar.class;}  
  public Object encode(XMLGregorianCalendar obj, Type type, BeanCoderContext context) {
    return new SimpleDateFormat(context.getOption().getDateFormat()).format(obj.toGregorianCalendar().getTime());
  }

  public XMLGregorianCalendar decode(Object obj, Type type, Object targetObj, BeanCoderContext context) {
    try{
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTime(CoderDate._decode(obj, context));
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    }catch(Exception e){
      throw new BeanCoderException(e);
    }
  }
}