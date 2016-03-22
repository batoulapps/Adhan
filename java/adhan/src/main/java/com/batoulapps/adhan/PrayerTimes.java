package com.batoulapps.adhan;

import com.batoulapps.adhan.internal.CalendricalHelper;
import com.batoulapps.adhan.internal.DoubleUtil;
import com.batoulapps.adhan.internal.SolarTime;
import com.batoulapps.adhan.internal.TimeComponents;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Year;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

public class PrayerTimes {
  public final LocalDateTime fajr;
  public final LocalDateTime sunrise;
  public final LocalDateTime dhuhr;
  public final LocalDateTime asr;
  public final LocalDateTime maghrib;
  public final LocalDateTime isha;

  public PrayerTimes(Coordinates coordinates, LocalDate date, CalculationParameters parameters) {
    ZonedDateTime tempFajr = null;
    ZonedDateTime tempSunrise = null;
    ZonedDateTime tempDhuhr = null;
    ZonedDateTime tempAsr = null;
    ZonedDateTime tempMaghrib = null;
    ZonedDateTime tempIsha = null;

    ZoneId timeZone = ZoneId.of("UTC");
    ZonedDateTime prayerDate = ZonedDateTime.of(date, LocalTime.MIN, timeZone);

    SolarTime solarTime = new SolarTime(date, coordinates);

    TimeComponents timeComponents = DoubleUtil.timeComponents(solarTime.transit);
    LocalDateTime transit = timeComponents == null ? null : timeComponents.dateComponents(date);

    timeComponents = DoubleUtil.timeComponents(solarTime.sunrise);
    LocalDateTime sunriseComponents = timeComponents == null ?
        null : timeComponents.dateComponents(date);

    timeComponents = DoubleUtil.timeComponents(solarTime.sunset);
    LocalDateTime sunsetComponents = timeComponents == null ?
        null : timeComponents.dateComponents(date);

    boolean error = transit == null || sunriseComponents == null || sunsetComponents == null;
    if (!error) {
      ZonedDateTime sunriseDate = ZonedDateTime.of(sunriseComponents, timeZone);
      ZonedDateTime sunsetDate = ZonedDateTime.of(sunsetComponents, timeZone);

      tempDhuhr = ZonedDateTime.of(transit, timeZone);
      tempSunrise = sunriseDate;
      tempMaghrib = sunsetDate;

      timeComponents = DoubleUtil.timeComponents(
          solarTime.afternoon(parameters.madhab.getShadowLength()));
      if (timeComponents != null) {
        LocalDateTime when = timeComponents.dateComponents(date);
        tempAsr = ZonedDateTime.of(when, timeZone);
      }

      timeComponents = DoubleUtil.timeComponents(solarTime.hourAngle(-parameters.fajrAngle, false));
      if (timeComponents != null) {
        LocalDateTime when = timeComponents.dateComponents(date);
        tempFajr = ZonedDateTime.of(when, timeZone);
      }

      // get night length
      ZonedDateTime tomorrowSunrise = sunriseDate.plus(1, ChronoUnit.DAYS);
      Duration night = Duration.between(sunsetDate, tomorrowSunrise);

      final ZonedDateTime safeFajr;
      if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
        if (coordinates.latitude < 55) {
          int dayOfYear = prayerDate.getDayOfYear();
          safeFajr = seasonAdjustedFajr(
              coordinates.latitude, dayOfYear, date.getYear(), sunriseDate);
        } else {
          Duration nightFraction = night.dividedBy(7);
          safeFajr = sunriseDate.minus(nightFraction);
        }
      } else {
        double portion = parameters.nightPortions().first;
        long nightFraction = (long) (portion * night.getSeconds());
        safeFajr = sunriseDate.minus(nightFraction, ChronoUnit.SECONDS);
      }

      if (tempFajr == null || (safeFajr != null && tempFajr.isBefore(safeFajr))) {
        tempFajr = safeFajr;
      }

      // Isha calculation with check against safe value
      if (parameters.ishaInterval > 0) {
        tempIsha = tempMaghrib.plus(parameters.ishaInterval * 60, ChronoUnit.SECONDS);
      } else {
        timeComponents = DoubleUtil.timeComponents(
            solarTime.hourAngle(-parameters.ishaAngle, true));
        if (timeComponents != null) {
          LocalDateTime when = timeComponents.dateComponents(date);
          tempIsha = ZonedDateTime.of(when, timeZone);
        }

        final ZonedDateTime safeIsha;
        if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
          if (coordinates.latitude < 55) {
            int dayOfYear = prayerDate.getDayOfYear();
            safeIsha = PrayerTimes.seasonAdjustedIsha(
                coordinates.latitude, dayOfYear, date.getYear(), sunsetDate);
          } else {
            Duration nightFraction = night.dividedBy(7);
            safeIsha = sunsetDate.plus(nightFraction);
          }
        } else {
          double portion = parameters.nightPortions().second;
          long nightFraction = (long) (portion * night.getSeconds());
          safeIsha = sunsetDate.plus(nightFraction, ChronoUnit.SECONDS);
        }

        if (tempIsha == null || (safeIsha != null && tempIsha.isAfter(safeIsha))) {
          tempIsha = safeIsha;
        }
      }
    }

    // method based offsets
    final Duration dhuhrOffset;
    if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
      // Moonsighting Committee requires 5 minutes for the sun to pass
      // the zenith and dhuhr to enter
      dhuhrOffset = Duration.ofMinutes(5);
    } else {
      dhuhrOffset = Duration.ofMinutes(1);
    }

    final Duration maghribOffset;
    if (parameters.method == CalculationMethod.MOON_SIGHTING_COMMITTEE) {
      // Moonsighting Committee adds 3 minutes to sunset time to account for light refraction
      maghribOffset = Duration.ofMinutes(3);
    } else {
      maghribOffset = Duration.ZERO;
    }

    if (error || tempFajr == null || tempAsr == null || tempIsha == null) {
      // if we don't have all prayer times then initialization failed
      this.fajr = null;
      this.sunrise = null;
      this.dhuhr = null;
      this.asr = null;
      this.maghrib = null;
      this.isha = null;
    } else {
      // Assign final times to public struct members with all offsets
      this.fajr = CalendricalHelper.roundedMinute(
          tempFajr.plus(parameters.adjustments.fajr, ChronoUnit.MINUTES).toLocalDateTime());
      this.sunrise = CalendricalHelper.roundedMinute(
          tempSunrise.plus(parameters.adjustments.sunrise, ChronoUnit.MINUTES).toLocalDateTime());
      this.dhuhr = CalendricalHelper.roundedMinute(
          tempDhuhr.plus(parameters.adjustments.dhuhr, ChronoUnit.MINUTES)
              .plus(dhuhrOffset).toLocalDateTime());
      this.asr = CalendricalHelper.roundedMinute(
          tempAsr.plus(parameters.adjustments.asr, ChronoUnit.MINUTES).toLocalDateTime());
      this.maghrib = CalendricalHelper.roundedMinute(
          tempMaghrib.plus(parameters.adjustments.maghrib, ChronoUnit.MINUTES)
              .plus(maghribOffset).toLocalDateTime());
      this.isha = CalendricalHelper.roundedMinute(
          tempIsha.plus(parameters.adjustments.isha, ChronoUnit.MINUTES).toLocalDateTime());
    }
  }

  private static ZonedDateTime seasonAdjustedFajr(
      double latitude, int day, int year, ZonedDateTime sunrise) {
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

    return sunrise.minus((long) Math.floor(adjustment), ChronoUnit.MINUTES);
  }

  private static ZonedDateTime seasonAdjustedIsha(
      double latitude, int day, int year, ZonedDateTime sunset) {
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

    return sunset.plus((long) Math.ceil(adjustment), ChronoUnit.MINUTES);
  }

  static int daysSinceSolstice(int dayOfYear, int year, double latitude) {
    int daysSinceSolistice = 0;
    final int northernOffset = 10;
    boolean isLeapYear = Year.isLeap(year);
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
