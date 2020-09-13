package com.jsonex.core.type;

import lombok.AllArgsConstructor;
import lombok.Data;

public class Tuple {
  @Data @AllArgsConstructor(staticName = "of") public static class Pair<T1, T2> { public T1 _1; public T2 _2; }
  @Data @AllArgsConstructor(staticName = "of") public static class _3<T1, T2, T3> { public T1 _1; public T2 _2; public T3 _3; }
  @Data @AllArgsConstructor(staticName = "of") public static class _4<T1, T2, T3, T4> { public T1 _1; public T2 _2; public T3 _3; public T4 _4; }
  @Data @AllArgsConstructor(staticName = "of") public static class _5<T1, T2, T3, T4, T5> { public T1 _1; public T2 _2; public T3 _3; public T4 _4; public T5 _5; }
  @Data @AllArgsConstructor(staticName = "of") public static class _6<T1, T2, T3, T4, T5, T6> { public T1 _1; public T2 _2; public T3 _3; public T4 _4; public T5 _5; public T6 _6; }
  @Data @AllArgsConstructor(staticName = "of") public static class _7<T1, T2, T3, T4, T5, T6, T7> { public T1 _1; public T2 _2; public T3 _3; public T4 _4; public T5 _5; public T6 _6; public T7 _7; }

  public static <T1, T2> Pair of(T1 _1, T2 _2) { return Pair.of(_1, _2); }
  public static <T1, T2, T3>  _3 of(T1 _1, T2 _2, T3 _3) { return Tuple._3.of(_1, _2, _3); }
  public static <T1, T2, T3, T4>  _4 of(T1 _1, T2 _2, T3 _3, T4 _4) { return Tuple._4.of(_1, _2, _3, _4); }
  public static <T1, T2, T3, T4, T5>  _5 of(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5) { return Tuple._5.of(_1, _2, _3, _4, _5); }
  public static <T1, T2, T3, T4, T5, T6>  _6 of(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6) { return Tuple._6.of(_1, _2, _3, _4, _5, _6); }
  public static <T1, T2, T3, T4, T5, T6, T7>  _7 of(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7) { return Tuple._7.of(_1, _2, _3, _4, _5, _6, _7); }
}
