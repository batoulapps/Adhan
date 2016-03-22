package com.batoulapps.adhan;

import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import static com.google.common.truth.Truth.assertThat;

public class PrayerTimesTest {

  @Test
  public void testDaysSinceSolstice() {
    daysSinceSolsticeTest(11, /* year */ 2016, /* month */ 1, /* day */ 1, /* latitude */ 1);
    daysSinceSolsticeTest(10, /* year */ 2015, /* month */ 12, /* day */ 31, /* latitude */ 1);
    daysSinceSolsticeTest(10, /* year */ 2016, /* month */ 12, /* day */ 31, /* latitude */ 1);
    daysSinceSolsticeTest(0, /* year */ 2016, /* month */ 12, /* day */ 21, /* latitude */ 1);
    daysSinceSolsticeTest(1, /* year */ 2016, /* month */ 12, /* day */ 22, /* latitude */ 1);
    daysSinceSolsticeTest(71, /* year */ 2016, /* month */ 3, /* day */ 1, /* latitude */ 1);
    daysSinceSolsticeTest(70, /* year */ 2015, /* month */ 3, /* day */ 1, /* latitude */ 1);
    daysSinceSolsticeTest(365, /* year */ 2016, /* month */ 12, /* day */ 20, /* latitude */ 1);
    daysSinceSolsticeTest(364, /* year */ 2015, /* month */ 12, /* day */ 20, /* latitude */ 1);

    daysSinceSolsticeTest(0, /* year */ 2015, /* month */ 6, /* day */ 21, /* latitude */ -1);
    daysSinceSolsticeTest(0, /* year */ 2016, /* month */ 6, /* day */ 21, /* latitude */ -1);
    daysSinceSolsticeTest(364, /* year */ 2015, /* month */ 6, /* day */ 20, /* latitude */ -1);
    daysSinceSolsticeTest(365, /* year */ 2016, /* month */ 6, /* day */ 20, /* latitude */ -1);
  }

  private void daysSinceSolsticeTest(int value, int year, int month, int day, double latitude) {
    // For Northern Hemisphere start from December 21
    // (DYY=0 for December 21, and counting forward, DYY=11 for January 1 and so on).
    // For Southern Hemisphere start from June 21
    // (DYY=0 for June 21, and counting forward)
    LocalDate date = LocalDate.of(year, month, day);
    int dayOfYear = date.getDayOfYear();
    assertThat(PrayerTimes.daysSinceSolstice(dayOfYear, date.getYear(), latitude)).isEqualTo(value);
  }

  @Test
  public void testPrayerTimes() {
    LocalDate date = LocalDate.of(2015, 7, 12);
    CalculationParameters params = CalculationMethod.NORTH_AMERICA.getParameters();
    params.madhab = Madhab.HANAFI;

    Coordinates coordinates = new Coordinates(35.7750, -78.6336);
    PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, params);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(ZoneId.of("America/New_York"));
    ZoneId utc = ZoneId.of("UTC");

    assertThat(prayerTimes.fajr).isNotNull();
    assertThat(prayerTimes.sunrise).isNotNull();
    assertThat(prayerTimes.dhuhr).isNotNull();
    assertThat(prayerTimes.asr).isNotNull();
    assertThat(prayerTimes.maghrib).isNotNull();
    assertThat(prayerTimes.isha).isNotNull();

    assertThat(ZonedDateTime.of(prayerTimes.fajr, utc).format(formatter)).isEqualTo("04:42 AM");
    assertThat(ZonedDateTime.of(prayerTimes.sunrise, utc).format(formatter)).isEqualTo("06:08 AM");
    assertThat(ZonedDateTime.of(prayerTimes.dhuhr, utc).format(formatter)).isEqualTo("01:21 PM");
    assertThat(ZonedDateTime.of(prayerTimes.asr, utc).format(formatter)).isEqualTo("06:22 PM");
    assertThat(ZonedDateTime.of(prayerTimes.maghrib, utc).format(formatter)).isEqualTo("08:32 PM");
    assertThat(ZonedDateTime.of(prayerTimes.isha, utc).format(formatter)).isEqualTo("09:57 PM");
  }

  @Test
  public void testOffsets() {
    LocalDate date = LocalDate.of(2015, 12, 1);
    Coordinates coordinates = new Coordinates(35.7750, -78.6336);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(ZoneId.of("America/New_York"));
    ZoneId utc = ZoneId.of("UTC");

    CalculationParameters parameters = CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters();

    PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, parameters);
    assertThat(prayerTimes.fajr).isNotNull();
    assertThat(prayerTimes.sunrise).isNotNull();
    assertThat(prayerTimes.dhuhr).isNotNull();
    assertThat(prayerTimes.asr).isNotNull();
    assertThat(prayerTimes.maghrib).isNotNull();
    assertThat(prayerTimes.isha).isNotNull();

    assertThat(ZonedDateTime.of(prayerTimes.fajr, utc).format(formatter)).isEqualTo("05:35 AM");
    assertThat(ZonedDateTime.of(prayerTimes.sunrise, utc).format(formatter)).isEqualTo("07:06 AM");
    assertThat(ZonedDateTime.of(prayerTimes.dhuhr, utc).format(formatter)).isEqualTo("12:05 PM");
    assertThat(ZonedDateTime.of(prayerTimes.asr, utc).format(formatter)).isEqualTo("02:42 PM");
    assertThat(ZonedDateTime.of(prayerTimes.maghrib, utc).format(formatter)).isEqualTo("05:01 PM");
    assertThat(ZonedDateTime.of(prayerTimes.isha, utc).format(formatter)).isEqualTo("06:26 PM");

    parameters.adjustments.fajr = 10;
    parameters.adjustments.sunrise = 10;
    parameters.adjustments.dhuhr = 10;
    parameters.adjustments.asr = 10;
    parameters.adjustments.maghrib = 10;
    parameters.adjustments.isha = 10;

    prayerTimes = new PrayerTimes(coordinates, date, parameters);
    assertThat(prayerTimes.fajr).isNotNull();
    assertThat(prayerTimes.sunrise).isNotNull();
    assertThat(prayerTimes.dhuhr).isNotNull();
    assertThat(prayerTimes.asr).isNotNull();
    assertThat(prayerTimes.maghrib).isNotNull();
    assertThat(prayerTimes.isha).isNotNull();

    assertThat(ZonedDateTime.of(prayerTimes.fajr, utc).format(formatter)).isEqualTo("05:45 AM");
    assertThat(ZonedDateTime.of(prayerTimes.sunrise, utc).format(formatter)).isEqualTo("07:16 AM");
    assertThat(ZonedDateTime.of(prayerTimes.dhuhr, utc).format(formatter)).isEqualTo("12:15 PM");
    assertThat(ZonedDateTime.of(prayerTimes.asr, utc).format(formatter)).isEqualTo("02:52 PM");
    assertThat(ZonedDateTime.of(prayerTimes.maghrib, utc).format(formatter)).isEqualTo("05:11 PM");
    assertThat(ZonedDateTime.of(prayerTimes.isha, utc).format(formatter)).isEqualTo("06:36 PM");

    parameters.adjustments = new PrayerAdjustments();
    prayerTimes = new PrayerTimes(coordinates, date, parameters);
    assertThat(prayerTimes.fajr).isNotNull();
    assertThat(prayerTimes.sunrise).isNotNull();
    assertThat(prayerTimes.dhuhr).isNotNull();
    assertThat(prayerTimes.asr).isNotNull();
    assertThat(prayerTimes.maghrib).isNotNull();
    assertThat(prayerTimes.isha).isNotNull();

    assertThat(ZonedDateTime.of(prayerTimes.fajr, utc).format(formatter)).isEqualTo("05:35 AM");
    assertThat(ZonedDateTime.of(prayerTimes.sunrise, utc).format(formatter)).isEqualTo("07:06 AM");
    assertThat(ZonedDateTime.of(prayerTimes.dhuhr, utc).format(formatter)).isEqualTo("12:05 PM");
    assertThat(ZonedDateTime.of(prayerTimes.asr, utc).format(formatter)).isEqualTo("02:42 PM");
    assertThat(ZonedDateTime.of(prayerTimes.maghrib, utc).format(formatter)).isEqualTo("05:01 PM");
    assertThat(ZonedDateTime.of(prayerTimes.isha, utc).format(formatter)).isEqualTo("06:26 PM");
  }

  @Test
  public void testMoonsightingMethod() {
    LocalDate date = LocalDate.of(2106, 1, 31);
    Coordinates coordinates = new Coordinates(35.7750, -78.6336);
    PrayerTimes prayerTimes = new PrayerTimes(
        coordinates, date, CalculationMethod.MOON_SIGHTING_COMMITTEE.getParameters());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(ZoneId.of("America/New_York"));
    ZoneId utc = ZoneId.of("UTC");

    assertThat(prayerTimes.fajr).isNotNull();
    assertThat(prayerTimes.sunrise).isNotNull();
    assertThat(prayerTimes.dhuhr).isNotNull();
    assertThat(prayerTimes.asr).isNotNull();
    assertThat(prayerTimes.maghrib).isNotNull();
    assertThat(prayerTimes.isha).isNotNull();

    assertThat(ZonedDateTime.of(prayerTimes.fajr, utc).format(formatter)).isEqualTo("05:48 AM");
    assertThat(ZonedDateTime.of(prayerTimes.sunrise, utc).format(formatter)).isEqualTo("07:16 AM");
    assertThat(ZonedDateTime.of(prayerTimes.dhuhr, utc).format(formatter)).isEqualTo("12:33 PM");
    assertThat(ZonedDateTime.of(prayerTimes.asr, utc).format(formatter)).isEqualTo("03:20 PM");
    assertThat(ZonedDateTime.of(prayerTimes.maghrib, utc).format(formatter)).isEqualTo("05:43 PM");
    assertThat(ZonedDateTime.of(prayerTimes.isha, utc).format(formatter)).isEqualTo("07:05 PM");
  }
}
