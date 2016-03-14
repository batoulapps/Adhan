package com.batoulapps.adhan.internal;

class DoubleUtil {

  static double normalizeWithBound(double value, double max) {
    return value - (max * (Math.floor(value / max)));
  }

  static double unwindAngle(double value) {
    return normalizeWithBound(value, 360);
  }
}
