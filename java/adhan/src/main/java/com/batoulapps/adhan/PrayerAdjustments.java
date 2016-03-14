package com.batoulapps.adhan;

/**
 * Adjustment value for prayer times, in minutes
 */
public class PrayerAdjustments {
  public int fajr;
  public int sunrise;
  public int dhuhr;
  public int asr;
  public int maghrib;
  public int isha;

  public PrayerAdjustments() {
    this(0, 0, 0, 0, 0, 0);
  }

  public PrayerAdjustments(int fajr, int sunrise, int dhuhr, int asr, int maghrib, int isha) {
    this.fajr = fajr;
    this.sunrise = sunrise;
    this.dhuhr = dhuhr;
    this.asr = asr;
    this.maghrib = maghrib;
    this.isha = isha;
  }
}
