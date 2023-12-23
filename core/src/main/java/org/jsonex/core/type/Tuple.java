package org.jsonex.core.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Modeled as mutable and provide a default constructor so that simplify the subclasses logic */
public interface Tuple {
  @Data @AllArgsConstructor(staticName = "of") @RequiredArgsConstructor class Pair<T0, T1> implements Tuple { public T0 _0; public T1 _1; }
  @Data @AllArgsConstructor(staticName = "of") @RequiredArgsConstructor class Tuple3<T0, T1, T2> implements Tuple { public T0 _0; public T1 _1; public T2 _2; }
  @Data @AllArgsConstructor(staticName = "of") @RequiredArgsConstructor class Tuple4<T0, T1, T2, T3> implements Tuple { public T0 _0; public T1 _1; public T2 _2; public T3 _3; }
  @Data @AllArgsConstructor(staticName = "of") @RequiredArgsConstructor class Tuple5<T0, T1, T2, T3, T4> implements Tuple { public T0 _0; public T1 _1; public T2 _2; public T3 _3; public T4 _4; }
  @Data @AllArgsConstructor(staticName = "of") @RequiredArgsConstructor class Tuple6<T0, T1, T2, T3, T4, T5> implements Tuple { public T0 _0; public T1 _1; public T2 _2; public T3 _3; public T4 _4; public T5 _5; }
  @Data @AllArgsConstructor(staticName = "of") @RequiredArgsConstructor class Tuple7<T0, T1, T2, T3, T4, T5, T6> implements Tuple { public T0 _0; public T1 _1; public T2 _2; public T3 _3; public T4 _4; public T5 _5; public T6 _6; }

  static <T0, T1> Pair<T0, T1> of(T0 _0, T1 _1) { return Pair.of(_0, _1); }
  static <T0, T1, T2> Tuple3<T0, T1, T2> of(T0 _0, T1 _1, T2 _2) { return Tuple3.of(_0, _1, _2); }
  static <T0, T1, T2, T3> Tuple4<T0, T1, T2, T3> of(T0 _0, T1 _1, T2 _2, T3 _3) { return Tuple4.of(_0, _1, _2, _3); }
  static <T0, T1, T2, T3, T4> Tuple5<T0, T1, T2, T3, T4> of(T0 _0, T1 _1, T2 _2, T3 _3, T4 _4) { return Tuple5.of(_0, _1, _2, _3, _4); }
  static <T0, T1, T2, T3, T4, T5> Tuple6<T0, T1, T2, T3, T4, T5> of(T0 _0, T1 _1, T2 _2, T3 _3, T4 _4, T5 _5) { return Tuple6.of(_0, _1, _2, _3, _4, _5); }
  static <T0, T1, T2, T3, T4, T5, T6> Tuple7<T0, T1, T2, T3, T4, T5, T6> of(T0 _0, T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6) { return Tuple7.of(_0, _1, _2, _3, _4, _5, _6); }
}
