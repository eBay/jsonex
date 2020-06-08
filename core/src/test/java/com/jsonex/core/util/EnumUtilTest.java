/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.util;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.jsonex.core.annotation.DefaultEnum;
import com.jsonex.core.type.Identifiable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.EnumSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

public class EnumUtilTest {
  @SuppressWarnings("WeakerAccess")
  @RequiredArgsConstructor
  enum TestEnum implements Identifiable<Long> {
    @DefaultEnum
    unknown(0L),
    value1(1L),
    value2(2L),
    value3(4L),
    value4(8L),
    ;
    @Getter final Long id;

    public static TestEnum get(long id) { return EnumUtil.getEnumById(TestEnum.class, id); }
    public static TestEnum getByName(String name) { return EnumUtil.valueOf(TestEnum.class, name); }
    public static long toLong(EnumSet<TestEnum> enums) { return EnumUtil.toLong(enums); }
    public static EnumSet<TestEnum> toEnumSet(long val) { return EnumUtil.toEnumSet(val, TestEnum.class); }
  }

  enum TestEnumWithoutDefault { value1, value2 }

  enum TestEnumWithJacksonAnnotation { value0, @JsonEnumDefaultValue  value1, value2 }

  @Test public void testGetEnumById() {
    assertSame(TestEnum.value1, TestEnum.get(1));
    assertSame(TestEnum.value4, TestEnum.get(8));
    assertSame(TestEnum.unknown, TestEnum.get(9));
    assertSame(TestEnum.value3, EnumUtil.getEnumByIdString(TestEnum.class, "4"));
    assertSame(TestEnum.unknown, EnumUtil.getEnumByIdString(TestEnum.class, "100"));

    assertNull("Should return null if no defaultEnum is annotated and name doesn't match a value",
        EnumUtil.valueOf(TestEnumWithoutDefault.class, "unknown"));

    assertEquals("Should return default value annotated by JsonEnumDefaultValue if value doesn't match anyone",
        TestEnumWithJacksonAnnotation.value1,
        EnumUtil.valueOf(TestEnumWithJacksonAnnotation.class, "unknown"));
  }

  @Test public void testValueOf() {
    assertSame(TestEnum.value1, TestEnum.getByName("value1"));
    assertSame(TestEnum.value4, TestEnum.getByName("value4"));
    assertSame(TestEnum.unknown, TestEnum.getByName("value10"));
  }
  
  @Test public void testToEnumSet() {
    EnumSet<TestEnum> enums = EnumSet.of(TestEnum.value1, TestEnum.value2, TestEnum.value3);
    assertEquals(7, TestEnum.toLong(enums));
    assertEquals(enums, TestEnum.toEnumSet(7));
  }
}
