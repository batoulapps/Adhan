package com.batoulapps.adhan.internal;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
   * @param date the date
   * @return the julian day
   */
  static double julianDay(Date date) {
    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTime(date);
    return julianDay(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0);
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
  public static boolean isLeapYear(int year) {
    return year % 4 == 0 && !(year % 100 == 0 && year % 400 != 0);
  }

  /**
   * Date and time with a rounded minute
   * This returns a date with the seconds rounded and added to the minute
   * @param when the date and time
   * @return the date and time with 0 seconds and minutes including rounded seconds
   */
  public static Date roundedMinute(Date when) {
    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTime(when);
    double minute = calendar.get(Calendar.MINUTE);
    double second = calendar.get(Calendar.SECOND);
    calendar.set(Calendar.MINUTE, (int) (minute + Math.round(second / 60)));
    calendar.set(Calendar.SECOND, 0);
    return calendar.getTime();
  }

  /**
   * Gets a date for the particular date
   * @param year the year
   * @param month the month
   * @param day the day
   * @return the date with a time set to 00:00:00 at utc
   */
  public static Date resolveTime(int year, int month, int day) {
    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    //noinspection MagicConstant
    calendar.set(year, month - 1, day, 0, 0, 0);
    return calendar.getTime();
  }

  /**
   * Gets a date for the particular date
   * @param components the date components
   * @return the date with a time set to 00:00:00 at utc
   */
  public static Date resolveTime(DateComponents components) {
    return resolveTime(components.year, components.month, components.day);
  }

  /**
   * Add an offset to a particular day
   * @param when the original date
   * @param amount the amount to add
   * @param field the field to add it to (from {@link java.util.Calendar}'s fields).
   * @return the date with the offset added
   */
  public static Date add(Date when, int amount, int field) {
    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTime(when);
    calendar.add(field, amount);
    return calendar.getTime();
  }
}
