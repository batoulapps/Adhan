package com.batoulapps.adhan.internal;

import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.ShadowLength;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

class SolarTime {

  final LocalDate date;
  final Coordinates observer;
  final SolarCoordinates solar;
  final double transit;
  final double sunrise;
  final double sunset;

  private final SolarCoordinates prevSolar;
  private final SolarCoordinates nextSolar;
  private double approximateTransit;

  SolarTime(LocalDate date, Coordinates coordinates) {
    final LocalDate today = LocalDate.from(date);
    final LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
    final LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);

    this.prevSolar = new SolarCoordinates(CalendricalHelper.julianDay(yesterday));
    this.solar = new SolarCoordinates(CalendricalHelper.julianDay(today));
    this.nextSolar = new SolarCoordinates(CalendricalHelper.julianDay(tomorrow));

    this.approximateTransit = Astronomical.approximateTransit(coordinates.longitude,
        solar.apparentSiderealTime, solar.rightAscension);
    final double solarAltitude = -50.0 / 60.0;

    this.date = date;
    this.observer = coordinates;
    this.transit = Astronomical.correctedTransit(this.approximateTransit, coordinates.longitude,
        solar.apparentSiderealTime, solar.rightAscension, prevSolar.rightAscension,
        nextSolar.rightAscension);
    this.sunrise = Astronomical.correctedHourAngle(this.approximateTransit, solarAltitude,
        coordinates, false, solar.apparentSiderealTime, solar.rightAscension,
        prevSolar.rightAscension, nextSolar.rightAscension, solar.declination,
        prevSolar.declination, nextSolar.declination);
    this.sunset = Astronomical.correctedHourAngle(this.approximateTransit, solarAltitude,
        coordinates, true, solar.apparentSiderealTime, solar.rightAscension,
        prevSolar.rightAscension, nextSolar.rightAscension, solar.declination,
        prevSolar.declination, nextSolar.declination);
  }

  double hourAngle(double angle, boolean afterTransit) {
    return Astronomical.correctedHourAngle(this.approximateTransit, angle, this.observer,
        afterTransit, this.solar.apparentSiderealTime, this.solar.rightAscension,
        this.prevSolar.rightAscension, this.nextSolar.rightAscension, this.solar.declination,
        this.prevSolar.declination, this.nextSolar.declination);
  }

  // hours from transit
  double afternoon(ShadowLength shadowLength) {
    // TODO (from Swift version) source shadow angle calculation
    final double tangent = Math.abs(observer.latitude - solar.declination);
    final double inverse = shadowLength.getShadowLength() + Math.tan(Math.toRadians(tangent));
    final double angle = Math.toDegrees(Math.atan(1.0 / inverse));

    return hourAngle(angle, true);
  }

}
