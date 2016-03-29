package com.batoulapps.adhan.internal;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Year;
import org.threeten.bp.ZonedDateTime;

public class CalendricalHelper {

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
   * The Julian Day for a given date
   * @param localDate the date
   * @return the julian day
   */
  static double julianDay(LocalDate localDate) {
    return julianDay(localDate.getYear(),
        localDate.getMonthValue(), localDate.getDayOfMonth(), 0.0);
  }

  /**
   * The Julian Day for a given date and time
   * @param localDateTime the date and time
   * @return the julian day
   */
  static double julianDay(LocalDateTime localDateTime) {
    return julianDay(localDateTime.getYear(), localDateTime.getMonthValue(),
        localDateTime.getDayOfMonth(), localDateTime.getHour() + localDateTime.getMinute() / 60.0);
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

  /**
   * Julian century from the epoch.
   * @param JD the julian day
   * @return the julian century from the epoch
   */
  static double julianCentury(double JD) {
    /* Equation from Astronomical Algorithms page 163 */
    return (JD - 2451545.0) / 36525;
  }

  /**
   * Whether or not a year is a leap year (has 366 days)
   * @param year the year
   * @return whether or not its a leap year
   */
  static boolean isLeapYear(int year) {
    return Year.isLeap(year);
  }

  /**
   * Zoned date and time with a rounded minute
   * This returns a date with the seconds rounded and added to the minute
   * @param when the date and time
   * @return the date and time with 0 seconds and minutes including rounded seconds
   */
  public static ZonedDateTime roundedMinute(ZonedDateTime when) {
    final double minute = when.getMinute();
    final double second = when.getSecond();
    return when.withMinute((int) (minute + Math.round(second / 60))).withSecond(0);
  }
}
