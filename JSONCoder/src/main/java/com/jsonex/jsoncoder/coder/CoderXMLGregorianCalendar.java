/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.jsoncoder.coder;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.BeanCoderException;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.util.GregorianCalendar;

public class CoderXMLGregorianCalendar implements ICoder<XMLGregorianCalendar>{
  public static final InjectableInstance<CoderXMLGregorianCalendar> it = InjectableInstance.of(CoderXMLGregorianCalendar.class);
  public static CoderXMLGregorianCalendar get() { return it.get(); }

  @Override public Class<XMLGregorianCalendar> getType() { return XMLGregorianCalendar.class; }
  @Override public TDNode encode(XMLGregorianCalendar obj, Type type, BeanCoderContext ctx, TDNode target) {
    String str = ctx.getCachedDateFormat(ctx.getOption().getDateFormat()).format(obj.toGregorianCalendar().getTime());
    return target.setValue(str);
  }

  public XMLGregorianCalendar decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context) {
    try {
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTime(CoderDate._decode(jsonNode.getValue(), context));
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    } catch(Exception e) {
      throw new BeanCoderException(e);
    }
  }
}