import datetime as dt
import math
from adhan_batoulapps.adhan import SolarCoordinates, julian_day, julian_century, mean_solar_longitude, mean_obliquity_of_the_ecliptic, \
    apparent_obliquity_of_the_ecliptic, mean_solar_anomaly, solar_equation_of_the_center, apparent_solar_longitude, \
    mean_sidereal_time, ascending_lunar_node_longitude, mean_lunar_longitude, nutation_in_longitude, nutation_in_obliquity, \
    altitude_of_celestial_body, approximate_transit, corrected_transit, corrected_hour_angle, Coordinates, SolarTime, \
    rounded_minute, date_with_hours, interpolate, interpolate_angles, is_leap_year, days_since_solstice, unwind_angle_360


def test_solar_coordinates_1():
    # values from Astronomical Algorithms page 165
    jd = julian_day(dt.datetime(1992, 10, 13))
    solar = SolarCoordinates(jd)

    T = julian_century(jd)
    L0 = mean_solar_longitude(T)
    ε0 = mean_obliquity_of_the_ecliptic(T)
    εapp = apparent_obliquity_of_the_ecliptic(T, ε0)
    M = mean_solar_anomaly(T)
    C = solar_equation_of_the_center(T, M)
    λ = apparent_solar_longitude(T, L0)
    δ = solar.declination
    α = unwind_angle_360(solar.right_ascension)

    assert math.isclose(T, -0.072183436, abs_tol=0.00000000001)
    assert math.isclose(L0, 201.80720, abs_tol=0.00001)
    assert math.isclose(ε0, 23.44023, abs_tol=0.00001)
    assert math.isclose(εapp, 23.43999, abs_tol=0.00001)
    assert math.isclose(M, 278.99397, abs_tol=0.00001)
    assert math.isclose(C, -1.89732, abs_tol=0.00001)

    # lower accuracy than desired
    assert math.isclose(λ, 199.90895, abs_tol=0.00002)
    assert math.isclose(δ, -7.78507, abs_tol=0.00001)
    assert math.isclose(α, 198.38083, abs_tol=0.00001)


def test_solar_coordinates_2():
    # values from Astronomical Algorithms page 88

    jd = julian_day(dt.datetime(1987, 4, 10))
    solar = SolarCoordinates(jd)

    T = julian_century(jd)
    θ0 = mean_sidereal_time(T)
    θapp = solar.apparent_sidereal_time
    Ω = ascending_lunar_node_longitude(T)
    ε0 = mean_obliquity_of_the_ecliptic(T)
    L0 = mean_solar_longitude(T)
    Lp = mean_lunar_longitude(T)
    ΔΨ = nutation_in_longitude(L0, Lp, Ω)
    Δε = nutation_in_obliquity(L0, Lp, Ω)
    ε = ε0 + Δε

    assert math.isclose(θ0, 197.693195, abs_tol=0.000001)
    assert math.isclose(θapp, 197.6922295833, abs_tol=0.0001)

    # values from Astronomical Algorithms page 148

    assert math.isclose(Ω, 11.2531, abs_tol=0.0001)
    assert math.isclose(ΔΨ, -0.0010522, abs_tol=0.0001)
    assert math.isclose(Δε, 0.0026230556, abs_tol=0.00001)
    assert math.isclose(ε0, 23.4409463889, abs_tol=0.000001)
    assert math.isclose(ε, 23.4435694444, abs_tol=0.00001)


def test_altitude_of_celestial_body():
    φ = 38 + (55 / 60) + (17.0 / 3600)
    δ = -6 - (43 / 60) - (11.61 / 3600)
    H = 64.352133
    h = altitude_of_celestial_body(φ, δ, H)

    assert math.isclose(h, 15.1249, abs_tol=0.0001)


def test_solar_time_transit():
    date = dt.date(2016, 5, 1)
    coordinates = Coordinates(51.507194, -0.116711)
    solar_time = SolarTime(date, coordinates)

    assert math.isclose(solar_time.transit, 11.9586053947969, abs_tol=0.00000000001)


def test_transit_and_hour_angle():
    # values from Astronomical Algorithms page 103

    longitude = -71.0833
    Θ = 177.74208
    α1 = 40.68021
    α2 = 41.73129
    α3 = 42.78204
    m0 = approximate_transit(longitude, Θ, α2)

    assert math.isclose(m0, 0.81965, abs_tol=0.00001)

    transit = corrected_transit(m0, longitude, Θ, α2, α1, α3) / 24

    assert math.isclose(transit, 0.81980, abs_tol=0.00001)

    δ1 = 18.04761
    δ2 = 18.44092
    δ3 = 18.82742

    rise = corrected_hour_angle(m0, -0.5667, Coordinates(42.3333, longitude), False, Θ, α2, α1, α3, δ2, δ1, δ3) / 24

    assert math.isclose(rise, 0.51766, abs_tol=0.00001)


def test_solar_time():
    # Comparison values generated from http://aa.usno.navy.mil/rstt/onedaytable?form=1&ID=AA&year=2015&month=7&day=12&state=NC&place=raleigh

    date = dt.datetime(2015, 7, 12)
    coordinates = Coordinates(35 + 47 / 60, -78 - 39 / 60)
    solar = SolarTime(date, coordinates)

    transit = solar.transit
    sunrise = solar.sunrise
    sunset = solar.sunset
    twilight_start = solar.hour_angle(-6, False)
    twilight_end = solar.hour_angle(-6, True)
    invalid = solar.hour_angle(-36, True)

    assert rounded_minute(date_with_hours(date, twilight_start)) == date + dt.timedelta(hours=9, minutes=38)
    assert rounded_minute(date_with_hours(date, sunrise)) == date + dt.timedelta(hours=10, minutes=8)
    assert rounded_minute(date_with_hours(date, transit)) == date + dt.timedelta(hours=17, minutes=20)
    assert rounded_minute(date_with_hours(date, sunset)) == date + dt.timedelta(hours=24, minutes=32)
    assert rounded_minute(date_with_hours(date, twilight_end)) == date + dt.timedelta(hours=25, minutes=2)
    assert date_with_hours(date, invalid) is None


def text_right_ascension_edge_case_thoroughly():
    coordinates = Coordinates(35 + 47 / 60, -78 - 39 / 60)
    solar = map(lambda x: SolarTime(dt.datetime(2016, 1, 1) + dt.timedelta(x), coordinates), range(0, 365))
    previous = None
    for index, current in enumerate(solar):
        if previous is not None:
            # transit from one day to another should not differ more than one minute
            assert math.fabs(current.transit - previous.transit) < 1 / 60

            # sunrise and sunset from one day to another should not differ more than two minutes
            assert math.fabs(current.sunrise - previous.sunrise) < 2 / 60
            assert math.fabs(current.sunset - previous.sunset) < 2 / 60


def test_calendrical_date_1():
    # generated from http://aa.usno.navy.mil/data/docs/RS_OneYear.php for KUKUIHAELE, HAWAII

    coordinates = Coordinates(20 + 7 / 60, -155 - 34 / 60)
    date = dt.datetime(2015, 4, 2)
    solar = SolarTime(date, coordinates)
    sunrise = solar.sunrise

    assert rounded_minute(date_with_hours(date, sunrise)) == date + dt.timedelta(hours=16, minutes=15)


def test_calendrical_date_2():
    # generated from http://aa.usno.navy.mil/data/docs/RS_OneYear.php for KUKUIHAELE, HAWAII

    coordinates = Coordinates(20 + 7 / 60, -155 - 34 / 60)
    date = dt.datetime(2015, 4, 3)
    solar = SolarTime(date, coordinates)
    sunrise = solar.sunrise

    assert rounded_minute(date_with_hours(date, sunrise)) == date + dt.timedelta(hours=16, minutes=14)


def test_interpolation():
    # values from Astronomical Algorithms page 25

    assert math.isclose(interpolate(0.877366, 0.884226, 0.870531, 4.35 / 24), 0.876125, abs_tol=0.000001)
    assert math.isclose(interpolate(1, -1, 3, 0.6), 2.2, abs_tol=0.000001)


def test_angle_interpolation():
    assert math.isclose(interpolate_angles(1, -1, 3, 0.6), 2.2, abs_tol=0.000001)
    assert math.isclose(interpolate_angles(1, 359, 3, 0.6), 2.2, abs_tol=0.000001)


def test_julian_day():
    # Comparison values generated from http://aa.usno.navy.mil/data/docs/JulianDate.php

    assert julian_day(dt.datetime(2010, 1, 2)) == 2455198.500000
    assert julian_day(dt.datetime(2011, 2, 4)) == 2455596.500000
    assert julian_day(dt.datetime(2012, 3, 6)) == 2455992.500000
    assert julian_day(dt.datetime(2013, 4, 8)) == 2456390.500000
    assert julian_day(dt.datetime(2014, 5, 10)) == 2456787.500000
    assert julian_day(dt.datetime(2015, 6, 12)) == 2457185.500000
    assert julian_day(dt.datetime(2016, 7, 14)) == 2457583.500000
    assert julian_day(dt.datetime(2017, 8, 16)) == 2457981.500000
    assert julian_day(dt.datetime(2018, 9, 18)) == 2458379.500000
    assert julian_day(dt.datetime(2019, 10, 20)) == 2458776.500000
    assert julian_day(dt.datetime(2020, 11, 22)) == 2459175.500000
    assert julian_day(dt.datetime(2021, 12, 24)) == 2459572.500000

    assert math.isclose(julian_day(dt.datetime(2015, 7, 12, 4, 15)), 2457215.67708333, abs_tol=0.000001)

    assert math.isclose(julian_day(dt.datetime(2015, 7, 12, 8)), 2457215.833333, abs_tol=0.000001)
    assert math.isclose(julian_day(dt.datetime(1992, 10, 13)), 2448908.5, abs_tol=0.000001)


def test_leap_year():
    assert is_leap_year(2015) is False
    assert is_leap_year(2016) is True
    assert is_leap_year(1600) is True
    assert is_leap_year(2000) is True
    assert is_leap_year(2400) is True
    assert is_leap_year(1700) is False
    assert is_leap_year(1800) is False
    assert is_leap_year(1900) is False
    assert is_leap_year(2100) is False
    assert is_leap_year(2200) is False
    assert is_leap_year(2300) is False
    assert is_leap_year(2500) is False
    assert is_leap_year(2600) is False


def testDaysSinceSolstice():
    # For Northern Hemisphere start from December 21
    # (DYY=0 for December 21, and counting forward, DYY=11 for January 1 and so on).
    # For Southern Hemisphere start from June 21
    # (DYY=0 for June 21, and counting forward)

    assert days_since_solstice(1, dt.datetime(2016, 1, 1)) == 11
    assert days_since_solstice(1, dt.datetime(2015, 12, 31)) == 10
    assert days_since_solstice(1, dt.datetime(2016, 12, 31)) == 10
    assert days_since_solstice(1, dt.datetime(2016, 12, 21)) == 0
    assert days_since_solstice(1, dt.datetime(2016, 12, 22)) == 1
    assert days_since_solstice(1, dt.datetime(2016, 3, 1)) == 71
    assert days_since_solstice(1, dt.datetime(2015, 3, 1)) == 70
    assert days_since_solstice(1, dt.datetime(2016, 12, 20)) == 365
    assert days_since_solstice(1, dt.datetime(2015, 12, 20)) == 364

    assert days_since_solstice(-1, dt.datetime(2015, 6, 21)) == 0
    assert days_since_solstice(-1, dt.datetime(2016, 6, 21)) == 0
    assert days_since_solstice(-1, dt.datetime(2015, 6, 20)) == 364
    assert days_since_solstice(-1, dt.datetime(2016, 6, 20)) == 365
