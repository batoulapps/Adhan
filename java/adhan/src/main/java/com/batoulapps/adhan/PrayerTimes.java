package com.batoulapps.adhan;

import com.batoulapps.adhan.internal.CalendricalHelper;
import com.batoulapps.adhan.internal.DateComponents;
import com.batoulapps.adhan.internal.DoubleUtil;
import com.batoulapps.adhan.internal.SolarTime;
import com.batoulapps.adhan.internal.TimeComponents;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class PrayerTimes {
  public final Date fajr;
  public final Date sunrise;
  public final Date dhuhr;
  public final Date asr;
  public final Date maghrib;
  public final Date isha;

  public PrayerTimes(Coordinates coordinates, DateComponents date, CalculationParameters params) {
    this(coordinates, CalendricalHelper.resolveTime(date), params);
  }

  private PrayerTimes(Coordinates coordinates, Date date, CalculationParameters parameters) {
    Date tempFajr = null;
    Date tempSunrise = null;
    Date tempDhuhr = null;
    Date tempAsr = null;
    Date tempMaghrib = null;
    Date tempIsha = null;

    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTime(date);

    final int year = calendar.get(Calendar.YEAR);
    final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

    SolarTime solarTime = new SolarTime(date, coordinates);

    TimeComponents timeComponents = DoubleUtil.timeComponents(solarTime.transit);
    Date transit = timeComponents == null ? null : timeComponents.dateComponents(date);

    timeComponents = DoubleUtil.timeComponents(solarTime.sunrise);
    Date sunriseComponents = timeComponents == null ? null : timeComponents.dateComponents(date);

    timeComponents = DoubleUtil.timeComponents(solarTime.sunset);
    Date sunsetComponents = timeComponents == null ? null : timeComponents.dateComponents(date);

    boolean error = transit == null || sunriseComponents == null || sunsetComponents == null;
    if (!error) {
      tempDhuhr = transit;
      tempSunrise = sunriseComponents;
      tempMaghrib = sunsetComponents;

      timeComponents = DoubleUtil.timeComponents(
          solarTime.afternoon(parameters.madhab.getShadowLength()));
      if (timeComponents != null) {
        tempAsr = timeComponents.dateComponents(date);
      }

      timeComponents = DoubleUtil.timeComponents(solarTime.hourAngle(-parameters.fajrAngle, false));
      if (timeComponents != null) {
        tempFajr = timeComponents.dateComponents(date);
      }

      // get night length
      Date tomorrowSunrise = CalendricalHelper.add(sunriseComponents, 1, Calendar.DAY_OF_YEAR);
      long night = tomorrowSunrise.getTime() - sunsetComponents.getTime();

      final Date safeFajr;
      if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
        if (coordinates.latitude < 55) {
          safeFajr = seasonAdjustedFajr(
              coordinates.latitude, dayOfYear, year, sunriseComponents);
        } else {
          safeFajr = CalendricalHelper.add(
              sunriseComponents, -1 * (int) (night / 7000), Calendar.SECOND);
        }
      } else {
        double portion = parameters.nightPortions().first;
        long nightFraction = (long) (portion * night / 1000);
        safeFajr = CalendricalHelper.add(
            sunriseComponents, -1 * (int) nightFraction, Calendar.SECOND);
      }

      if (tempFajr == null || tempFajr.before(safeFajr)) {
        tempFajr = safeFajr;
      }

      // Isha calculation with check against safe value
      if (parameters.ishaInterval > 0) {
        tempIsha = CalendricalHelper.add(
            tempMaghrib, parameters.ishaInterval * 60, Calendar.SECOND);
      } else {
        timeComponents = DoubleUtil.timeComponents(
            solarTime.hourAngle(-parameters.ishaAngle, true));
        if (timeComponents != null) {
          tempIsha = timeComponents.dateComponents(date);
        }

        final Date safeIsha;
        if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
          if (coordinates.latitude < 55) {
            safeIsha = PrayerTimes.seasonAdjustedIsha(
                coordinates.latitude, dayOfYear, year, sunsetComponents);
          } else {
            safeIsha = CalendricalHelper.add(sunsetComponents,
                (int) (night / 7000), Calendar.SECOND);
          }
        } else {
          double portion = parameters.nightPortions().second;
          long nightFraction = (long) (portion * night / 1000);
          safeIsha = CalendricalHelper.add(sunsetComponents, (int) nightFraction, Calendar.SECOND);
        }

        if (tempIsha == null || (tempIsha.after(safeIsha))) {
          tempIsha = safeIsha;
        }
      }
    }

    // method based offsets
    final int dhuhrOffsetInMinutes;
    if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
      // Moonsighting Committee requires 5 minutes for the sun to pass
      // the zenith and dhuhr to enter
      dhuhrOffsetInMinutes = 5;
    } else {
      dhuhrOffsetInMinutes = 1;
    }

    final int maghribOffsetInMinutes;
    if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
      // Moonsighting Committee adds 3 minutes to sunset time to account for light refraction
      maghribOffsetInMinutes = 3;
    } else {
      maghribOffsetInMinutes = 0;
    }

    if (error || tempAsr == null) {
      // if we don't have all prayer times then initialization failed
      this.fajr = null;
      this.sunrise = null;
      this.dhuhr = null;
      this.asr = null;
      this.maghrib = null;
      this.isha = null;
    } else {
      // Assign final times to public struct members with all offsets
      this.fajr = CalendricalHelper.roundedMinute(CalendricalHelper.add(
          tempFajr, parameters.adjustments.fajr, Calendar.MINUTE));
      this.sunrise = CalendricalHelper.roundedMinute(
          CalendricalHelper.add(tempSunrise, parameters.adjustments.sunrise, Calendar.MINUTE));
      this.dhuhr = CalendricalHelper.roundedMinute(CalendricalHelper.add(
          tempDhuhr, parameters.adjustments.dhuhr + dhuhrOffsetInMinutes, Calendar.MINUTE));
      this.asr = CalendricalHelper.roundedMinute(CalendricalHelper.add(
          tempAsr, parameters.adjustments.asr, Calendar.MINUTE));
      this.maghrib = CalendricalHelper.roundedMinute(CalendricalHelper.add(
          tempMaghrib, parameters.adjustments.maghrib + maghribOffsetInMinutes, Calendar.MINUTE));
      this.isha = CalendricalHelper.roundedMinute(CalendricalHelper.add(
          tempIsha, parameters.adjustments.isha, Calendar.MINUTE));
    }
  }

  private static Date seasonAdjustedFajr(double latitude, int day, int year, Date sunrise) {
    final double a = 75 + ((28.65 / 55.0) * Math.abs(latitude));
    final double b = 75 + ((19.44 / 55.0) * Math.abs(latitude));
    final double c = 75 + ((32.74 / 55.0) * Math.abs(latitude));
    final double d = 75 + ((48.10 / 55.0) * Math.abs(latitude));

    final double adjustment;
    final int dyy = PrayerTimes.daysSinceSolstice(day, year, latitude);
    if ( dyy < 91) {
      adjustment = a + ( b - a ) / 91.0 * dyy;
    } else if ( dyy < 137) {
      adjustment = b + ( c - b ) / 46.0 * ( dyy - 91 );
    } else if ( dyy < 183 ) {
      adjustment = c + ( d - c ) / 46.0 * ( dyy - 137 );
    } else if ( dyy < 229 ) {
      adjustment = d + ( c - d ) / 46.0 * ( dyy - 183 );
    } else if ( dyy < 275 ) {
      adjustment = c + ( b - c ) / 46.0 * ( dyy - 229 );
    } else {
      adjustment = b + ( a - b ) / 91.0 * ( dyy - 275 );
    }

    return CalendricalHelper.add(sunrise, -(int) Math.floor(adjustment), Calendar.MINUTE);
  }

  private static Date seasonAdjustedIsha(double latitude, int day, int year, Date sunset) {
    final double a = 75 + ((25.60 / 55.0) * Math.abs(latitude));
    final double b = 75 + ((2.050 / 55.0) * Math.abs(latitude));
    final double c = 75 - ((9.210 / 55.0) * Math.abs(latitude));
    final double d = 75 + ((6.140 / 55.0) * Math.abs(latitude));

    final double adjustment;
    final int dyy = PrayerTimes.daysSinceSolstice(day, year, latitude);
    if ( dyy < 91) {
      adjustment = a + ( b - a ) / 91.0 * dyy;
    } else if ( dyy < 137) {
      adjustment = b + ( c - b ) / 46.0 * ( dyy - 91 );
    } else if ( dyy < 183 ) {
      adjustment = c + ( d - c ) / 46.0 * ( dyy - 137 );
    } else if ( dyy < 229 ) {
      adjustment = d + ( c - d ) / 46.0 * ( dyy - 183 );
    } else if ( dyy < 275 ) {
      adjustment = c + ( b - c ) / 46.0 * ( dyy - 229 );
    } else {
      adjustment = b + ( a - b ) / 91.0 * ( dyy - 275 );
    }

    return CalendricalHelper.add(sunset, (int) Math.ceil(adjustment), Calendar.MINUTE);
  }

  static int daysSinceSolstice(int dayOfYear, int year, double latitude) {
    int daysSinceSolistice;
    final int northernOffset = 10;
    boolean isLeapYear = CalendricalHelper.isLeapYear(year);
    final int southernOffset = isLeapYear ? 173 : 172;
    final int daysInYear = isLeapYear ? 366 : 365;

    if (latitude >= 0) {
      daysSinceSolistice = dayOfYear + northernOffset;
      if (daysSinceSolistice >= daysInYear) {
        daysSinceSolistice = daysSinceSolistice - daysInYear;
      }
    } else {
      daysSinceSolistice = dayOfYear - southernOffset;
      if (daysSinceSolistice < 0) {
        daysSinceSolistice = daysSinceSolistice + daysInYear;
      }
    }
    return daysSinceSolistice;
  }
}
