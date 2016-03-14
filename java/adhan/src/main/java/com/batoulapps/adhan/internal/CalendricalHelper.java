package com.batoulapps.adhan.internal;

class CalendricalHelper {

  /**
   * The Julian Day for a given Gregorian date
   * @param year the year
   * @param month the month
   * @param day the day
   * @return the julian day
   */
  static double julianDay(int year, int month, int day) {
    return julianDay(year, month, day, 0.0);
  }

  /**
   * The Julian Day for a given Gregorian date
   * @param year the year
   * @param month the month
   * @param day the day
   * @param hours hours
   * @return the julian day
   */
  static double julianDay(int year, int month, int day, double hours) {
    /* Equation from Astronomical Algorithms page 60 */

    // NOTE: Integer conversion is done intentionally for the purpose of decimal truncation

    int Y = month > 2 ? year : year - 1;
    int M = month > 2 ? month : month + 12;
    double D = day + (hours / 24);

    int A = Y/100;
    int B = 2 - A + (A/4);

    int i0 = (int) (365.25 * (Y + 4716));
    int i1 = (int) (30.6001 * (M + 1));
    return i0 + i1 + D + B - 1524.5;
  }

  /* Julian century from the epoch. */
  static double julianCentury(double JD) {
    /* Equation from Astronomical Algorithms page 163 */
    return (JD - 2451545.0) / 36525;
  }

  /* Whether or not a year is a leap year (has 366 days). */
  static boolean isLeapYear(int year) {
    if (year % 4 != 0) {
      return false;
    }

    if (year % 100 == 0 && year % 400 != 0) {
      return false;
    }

    return true;
  }
}
