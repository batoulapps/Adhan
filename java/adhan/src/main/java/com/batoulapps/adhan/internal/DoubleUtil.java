package com.batoulapps.adhan.internal;

public class DoubleUtil {

  static double normalizeWithBound(double value, double max) {
    return value - (max * (Math.floor(value / max)));
  }

  static double unwindAngle(double value) {
    return normalizeWithBound(value, 360);
  }

  public static TimeComponents timeComponents(double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    }

    final double hours = Math.floor(value);
    final double minutes = Math.floor((value - hours) * 60.0);
    final double seconds = Math.floor((value - (hours + minutes / 60.0)) * 60 * 60);

    // intentionally not using LocalTime because hours can be >= 24.
    return new TimeComponents((int) hours, (int) minutes, (int) seconds);
  }

  static double closestAngle(double angle) {
    if (angle >= -180 && angle <= 180) {
      return angle;
    }
    return angle - (360 * Math.round(angle / 360));
  }
}
