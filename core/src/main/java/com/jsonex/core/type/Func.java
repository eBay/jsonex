package com.jsonex.core.type;

public interface Func {
  @FunctionalInterface interface _3<P1, P2, P3, R> extends Func { public R apply(P1 p1, P2 p2, P3 p3); }
  @FunctionalInterface interface _4<P1, P2, P3, P4, R> extends Func  { public R apply(P1 p1, P2 p2, P3 p3, P4 p4); }
  @FunctionalInterface interface _5<P1, P2, P3, P4, P5, R> extends Func  { public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5); }
  @FunctionalInterface interface _6<P1, P2, P3, P4, P5, P6, R> extends Func  { public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);}
  @FunctionalInterface interface _7<P1, P2, P3, P4, P5, P6, P7, R> extends Func  { public R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7); }
}
