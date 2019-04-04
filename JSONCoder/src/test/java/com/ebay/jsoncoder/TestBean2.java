/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncoder;

import com.ebay.jsoncodercore.annotation.DefaultEnum;
import com.ebay.jsoncodercore.type.Identifiable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.EnumMap;
import java.util.EnumSet;

@SuppressWarnings("CanBeFinal")
@Accessors(chain = true)
public class TestBean2 {
  enum Enum1 { @DefaultEnum value1, value2 }

  @RequiredArgsConstructor
  enum IdentifiableEnum implements Identifiable<Integer> {
    value1(12345),
    value2(200)
    ;
    @Getter private final Integer id;//NOPMD
  }

  @RequiredArgsConstructor
  enum EnumStr implements Identifiable<String>{
    value1("strEnumV1"),
    value2("strEnumV2")
    ;
    @Getter private final String id;
  }

  @Getter @Setter private String strField;
  @Getter @Setter private Enum1 enumField;
  @Getter @Setter private Object[] objs;
  public IdentifiableEnum enumField2;
  public EnumStr strEnum;
  public EnumSet<Enum1> enumSet = EnumSet.allOf(Enum1.class);
  public EnumMap<Enum1, String> enumMap = new EnumMap<>(Enum1.class);
  public TestBean testBean;

  TestBean2() {}
  public TestBean2(String str){strField = str;}
}
