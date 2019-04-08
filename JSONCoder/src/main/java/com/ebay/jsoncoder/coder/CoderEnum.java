/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder.coder;

import java.lang.reflect.Type;

import com.ebay.jsoncoder.BeanCoderContext;
import com.ebay.jsoncoder.treedoc.TDNode;
import com.ebay.jsoncodercore.type.Identifiable;
import com.ebay.jsoncodercore.util.ClassUtil;
import com.ebay.jsoncodercore.util.EnumUtil;
import com.ebay.jsoncoder.ICoder;

@SuppressWarnings("rawtypes")
public class CoderEnum implements ICoder<Enum> {
  @Override public Class<Enum> getType() {return Enum.class;}

  @Override public TDNode encode(Enum obj, Type type, BeanCoderContext context, TDNode target) { return target.setValue(encode(obj, context)); }

  private Object encode(Enum obj, BeanCoderContext context) {
    if(obj instanceof Identifiable){
      Identifiable<?> id = (Identifiable<?>) obj;
      if(context.getOption().isShowEnumName()){
        return id.getId() + "-" + obj.toString();
      }else
        return id.getId();
    }else
      return obj.toString();
  }



  @Override public Enum decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext context) {
    Integer intValue;
    String strValue = null;

    Object obj = jsonNode.getValue();
    if(obj instanceof Integer)
      intValue = (Integer) obj;
    else{
      strValue = (String) obj;
      int p = strValue.indexOf('-');
      if(p >= 0)
        strValue = strValue.substring(0, p).trim();

      try{
        intValue = Integer.valueOf(strValue);
      }catch(NumberFormatException e){
        intValue = null;  //Useless statement for find bug
      }
    }
    
    Class<?> cls = ClassUtil.getGenericClass(type);
    if(intValue != null)
      return getEnumByInt(cls, intValue);
    else
      return getEnumByString(cls, strValue); 
  }

  private static Enum<?> getEnumByInt(Class cls, int i){
    if(Identifiable.class.isAssignableFrom(cls))
      return EnumUtil.getEnumById(cls, i);
    return (Enum<?>)cls.getEnumConstants()[i];
  }
  
  private static Enum getEnumByString(Class cls, String i){
    if(Identifiable.class.isAssignableFrom(cls)){
      Enum value = EnumUtil.getEnumByIdString(cls, i);
      if(value != null) return value;
    }
    return EnumUtil.valueOf(cls, i);
  }
}
