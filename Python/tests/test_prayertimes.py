from nose.tools import raises
from prayertimeslib import julian_date, Coordinates, CalculationMethod, Madhab, PrayerTimes, NewSolarTime
import datetime as dt


def almost_equal(a, b, places=7):
    return round(abs(a-b), places) == 0


def test_julian_date():
    assert julian_date(dt.datetime(year=2010, month=1, day=2)) == 2455198.500000
    assert julian_date(dt.datetime(year=2011, month=2, day=4)) == 2455596.500000
    assert julian_date(dt.datetime(year=2012, month=3, day=6)) == 2455992.500000
    assert julian_date(dt.datetime(year=2013, month=4, day=8)) == 2456390.500000
    assert julian_date(dt.datetime(year=2014, month=5, day=10)) == 2456787.500000
    assert julian_date(dt.datetime(year=2015, month=6, day=12)) == 2457185.500000
    assert julian_date(dt.datetime(year=2016, month=7, day=14)) == 2457583.500000
    assert julian_date(dt.datetime(year=2017, month=8, day=16)) == 2457981.500000
    assert julian_date(dt.datetime(year=2018, month=9, day=18)) == 2458379.500000
    assert julian_date(dt.datetime(year=2019, month=10, day=20)) == 2458776.500000
    assert julian_date(dt.datetime(year=2020, month=11, day=22)) == 2459175.500000
    assert julian_date(dt.datetime(year=2021, month=12, day=24)) == 2459572.500000


@raises(Exception)
def test_julian_date_out_of_range_low():
    julian_date(dt.datetime(year=1800, month=1, day=1))


@raises(Exception)
def test_julian_date_out_of_range_high():
    julian_date(dt.datetime(year=2100, month=1, day=1))


def test_coordinates():
    coords = Coordinates(latitude=35, longitude=-78)
    assert coords.latitude == 35
    assert coords.longitude == -78


def test_new_solar_time():
    date = dt.datetime(1992, 10, 13)
    coords = Coordinates(0, 0)
    st = NewSolarTime(date, coords)
    assert almost_equal(st.julian_centuries_since_epoch, -0.072183436, 9)
    assert almost_equal(st.mean_opliquity_of_ecliptic, 23.44023, 5)
    assert almost_equal(st.mean_longitude_of_sun, 201.80720, 5)
    assert almost_equal(st.eccentricity_of_earth_orbit, 0.016711668, 9)
    assert almost_equal(st.mean_anomaly_of_sun, 278.99397, 5)
    assert almost_equal(st.y, 0.0430381, 7)
    assert almost_equal(st.equation_of_time, 0.059825572, 8)


def test_solar_time():
    pass
    date = dt.datetime(2015, 12, 28)
    coords = Coordinates(latitude=51.5, longitude=-0.13)
    solar_time = NewSolarTime(date=date, coordinates=coords)
#     assert solar_time.equation_of_time == -1.15 / 60
#     assert solar_time.declination == -23.3


def test_prayer_times():
    pass
#     date = dt.datetime(2015, 7, 12)
#     params = CalculationMethod.north_america.calculation_parameters()
#     params.madhab = Madhab.hanafi
#     coords = Coordinates(latitude=35.7750, longitude=-78.6336)
#     prayer_times = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)

#     assert prayer_times.fajr.strftime("%-I:%M %p") == "4:42 AM"
#     assert prayer_times.sunrise.strftime("%-I:%M %p") == "6:08 AM"
#     assert prayer_times.dhuhr.strftime("%-I:%M %p") == "1:21 PM"
#     assert prayer_times.asr.strftime("%-I:%M %p") == "6:22 PM"
#     assert prayer_times.maghrib.strftime("%-I:%M %p") == "8:32 PM"
#     assert prayer_times.isha.strftime("%-I:%M %p") == "9:57 PM"
