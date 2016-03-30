package com.batoulapps.adhan;

public class CalculationParameters {
  public CalculationMethod method = CalculationMethod.OTHER;
  public double fajrAngle;
  public double ishaAngle;
  public int ishaInterval;
  public Madhab madhab = Madhab.SHAFI;
  public HighLatitudeRule highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT;
  public PrayerAdjustments adjustments = new PrayerAdjustments();

  public CalculationParameters(double fajrAngle, double ishaAngle) {
    this.fajrAngle = fajrAngle;
    this.ishaAngle = ishaAngle;
  }

  public CalculationParameters(double fajrAngle, int ishaInterval) {
    this(fajrAngle, 0.0);
    this.ishaInterval = ishaInterval;
  }

  public CalculationParameters(double fajrAngle, double ishaAngle, CalculationMethod method) {
    this(fajrAngle, ishaAngle);
    this.method = method;
  }

  public CalculationParameters(double fajrAngle, int ishaInterval, CalculationMethod method) {
    this(fajrAngle, ishaInterval);
    this.method = method;
  }

  public Pair<Double, Double> nightPortions() {
    switch (this.highLatitudeRule) {
      case MIDDLE_OF_THE_NIGHT: {
        return new Pair<Double, Double>(1.0 / 2.0, 1.0 / 2.0);
      }
      case SEVENTH_OF_THE_NIGHT: {
        return new Pair<Double, Double>(1.0 / 7.0, 1.0 / 7.0);
      }
      case TWILIGHT_ANGLE: {
        return new Pair<Double, Double>(this.fajrAngle / 60.0, this.ishaAngle / 60.0);
      }
      default: {
        throw new IllegalArgumentException("Invalid high latitude rule");
      }
    }
  }
}
