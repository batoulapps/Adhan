package com.batoulapps.adhan.internal;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;

import static com.google.common.truth.Truth.assertThat;

public class MathTest {

  @Test
  public void testAngleConversion() {
    assertThat(Math.toDegrees(Math.PI)).isWithin(0.00001).of(180.0);
    assertThat(Math.toDegrees(Math.PI / 2)).isWithin(0.00001).of(90.0);
  }

  @Test
  public void testNormalizing() {
    assertThat(DoubleUtil.normalizeWithBound(2.0, -5)).isWithin(0.00001).of(-3);
    assertThat(DoubleUtil.normalizeWithBound(-4.0, -5.0)).isWithin(0.00001).of(-4);
    assertThat(DoubleUtil.normalizeWithBound(-6.0, -5.0)).isWithin(0.00001).of(-1);

    assertThat(DoubleUtil.normalizeWithBound(-1.0, 24)).isWithin(0.00001).of(23);
    assertThat(DoubleUtil.normalizeWithBound(1.0, 24.0)).isWithin(0.00001).of(1);
    assertThat(DoubleUtil.normalizeWithBound(49.0, 24)).isWithin(0.00001).of(1);

    assertThat(DoubleUtil.normalizeWithBound(361.0, 360)).isWithin(0.00001).of(1);
    assertThat(DoubleUtil.normalizeWithBound(360.0, 360)).isWithin(0.00001).of(0);
    assertThat(DoubleUtil.normalizeWithBound(259.0, 360)).isWithin(0.00001).of(259);
    assertThat(DoubleUtil.normalizeWithBound(2592.0, 360)).isWithin(0.00001).of(72);

    assertThat(DoubleUtil.unwindAngle(-45.0)).isWithin(0.00001).of(315);
    assertThat(DoubleUtil.unwindAngle(361.0)).isWithin(0.00001).of(1);
    assertThat(DoubleUtil.unwindAngle(360.0)).isWithin(0.00001).of(0);
    assertThat(DoubleUtil.unwindAngle(259.0)).isWithin(0.00001).of(259);
    assertThat(DoubleUtil.unwindAngle(2592.0)).isWithin(0.00001).of(72);
  }

  @Test
  public void testTimeComponents() {
    final TimeComponents comps1 = DoubleUtil.timeComponents(15.199);
    assertThat(comps1).isNotNull();
    assertThat(comps1.hours).isEqualTo(15);
    assertThat(comps1.minutes).isEqualTo(11);
    assertThat(comps1.seconds).isEqualTo(56);

    final TimeComponents comps2 = DoubleUtil.timeComponents(1.0084);
    assertThat(comps2).isNotNull();
    assertThat(comps2.hours).isEqualTo(1);
    assertThat(comps2.minutes).isEqualTo(0);
    assertThat(comps2.seconds).isEqualTo(30);

    final TimeComponents comps3 = DoubleUtil.timeComponents(1.0083);
    assertThat(comps3).isNotNull();
    assertThat(comps3.hours).isEqualTo(1);
    assertThat(comps3.minutes).isEqualTo(0);

    final TimeComponents comps4 = DoubleUtil.timeComponents(2.1);
    assertThat(comps4).isNotNull();
    assertThat(comps4.hours).isEqualTo(2);
    assertThat(comps4.minutes).isEqualTo(6);

    final TimeComponents comps5 = DoubleUtil.timeComponents(3.5);
    assertThat(comps5).isNotNull();
    assertThat(comps5.hours).isEqualTo(3);
    assertThat(comps5.minutes).isEqualTo(30);
  }

  @Test
  public void testMinuteRounding() {
    final LocalDateTime comps1 = LocalDateTime.of(2015, 1, 1, 10, 2, 29);
    final LocalDateTime rounded1 = CalendricalHelper.roundedMinute(comps1);
    assertThat(rounded1.getMinute()).isEqualTo(2);
    assertThat(rounded1.getSecond()).isEqualTo(0);

    final LocalDateTime comps2 = LocalDateTime.of(2015, 1, 1, 10, 2, 31);
    final LocalDateTime rounded2 = CalendricalHelper.roundedMinute(comps2);
    assertThat(rounded2.getMinute()).isEqualTo(3);
    assertThat(rounded2.getSecond()).isEqualTo(0);
  }
}
