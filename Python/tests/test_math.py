import datetime as dt
import math
from adhan import normalize, unwind_angle_360, unwind_angle_180, rounded_minute


def test_normalize():
    assert normalize(2, -5) == -3
    assert normalize(-4, -5) == -4
    assert normalize(-6, -5) == -1

    assert normalize(-1, 24) == 23
    assert normalize(1, 24) == 1
    assert normalize(49, 24) == 1

    assert normalize(361, 360) == 1
    assert normalize(360, 360) == 0
    assert normalize(259, 360) == 259
    assert normalize(2592, 360) == 72


def test_unwind_angle():
    assert unwind_angle_360(-45.0) == 315
    assert unwind_angle_360(361.0) == 1
    assert unwind_angle_360(360.0) == 0
    assert unwind_angle_360(259.0) == 259
    assert unwind_angle_360(2592.0) == 72

    assert math.isclose(unwind_angle_360(normalize(360.1, 360)), 0.1, abs_tol=0.01)

    assert unwind_angle_180(360.0) == 0
    assert unwind_angle_180(361.0) == 1
    assert unwind_angle_180(1.0) == 1
    assert unwind_angle_180(-1.0) == -1
    assert unwind_angle_180(-181.0) == 179
    assert unwind_angle_180(180.0) == 180
    assert unwind_angle_180(359.0) == -1
    assert unwind_angle_180(-359.0) == 1
    assert unwind_angle_180(1261.0) == -179

    assert math.isclose(unwind_angle_180(-360.1), -0.1, abs_tol=0.01)


def test_minute_rounding():
    date = rounded_minute(dt.datetime(2015, 1, 1, 10, 2, 29))
    assert date.minute == 2
    assert date.second == 0

    date = rounded_minute(dt.datetime(2015, 1, 1, 10, 2, 31))
    assert date.minute == 3
    assert date.second == 0