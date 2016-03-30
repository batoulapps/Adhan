package com.batoulapps.adhan.internal;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

public class TimeComponents {
  final int hours;
  final int minutes;
  final int seconds;

  TimeComponents(int hours, int minutes, int seconds) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  public LocalDateTime dateComponents(LocalDate date) {
    if (hours < 24) {
      LocalTime time = LocalTime.of(hours, minutes, seconds);
      return LocalDateTime.of(date, time);
    } else {
      return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
          23, minutes, seconds).plusHours(hours - 23);
    }
  }
}
