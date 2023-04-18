package org.jsonex.core.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface Union {
  @Data @AllArgsConstructor(access=AccessLevel.PROTECTED) class Union2<T0, T1> implements Union {
    public final T0 _0;
    public final T1 _1;
    public Class<?> getType() {
      if (_0 != null) return  _0.getClass();
      else return  _1.getClass();
    }

    public static <T0, T1> Union2<T0, T1> of_0(T0 _0) { return new Union2<>(_0, null); }
    public static <T0, T1> Union2<T0, T1> of_1(T1 _1) { return new Union2<>(null, _1); }
  }

  @Data @AllArgsConstructor(access=AccessLevel.PROTECTED) class Union3<T0, T1, T2> implements Union {
    public final T0 _0;
    public final T1 _1;
    public final T2 _2;
    public Class<?> getType() {
      if (_0 != null) return _0.getClass();
      if (_1 != null) return _1.getClass();
      else return  _2.getClass();
    }
    public static <T0, T1, T2> Union3<T0, T1, T2> of_0(T0 _0) { return new Union3<>(_0, null, null); }
    public static <T0, T1, T2> Union3<T0, T1, T2> of_1(T1 _1) { return new Union3<>(null, _1, null); }
    public static <T0, T1, T2> Union3<T0, T1, T2> of_2(T2 _2) { return new Union3<>(null, null, _2); }
  }
}
