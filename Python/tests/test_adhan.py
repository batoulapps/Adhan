import datetime as dt
from adhan_batoulapps import CalculationParameters, HighLatitudeRule, CalculationMethod, PrayerTimes, Madhab, Coordinates, PrayerAdjustments


def test_night_portion():
    params = CalculationParameters(fajr_angle=18, isha_angle=18)
    params.high_latitude_rule = HighLatitudeRule.middle_of_the_night
    assert params.night_portions()[0] == 0.5
    assert params.night_portions()[1] == 0.5

    params = CalculationParameters(fajr_angle=18, isha_angle=18)
    params.high_latitude_rule = HighLatitudeRule.seventh_of_the_night
    assert params.night_portions()[0] == 1 / 7
    assert params.night_portions()[1] == 1 / 7

    params = CalculationParameters(fajr_angle=10, isha_angle=15)
    params.high_latitude_rule = HighLatitudeRule.twilight_angle
    assert params.night_portions()[0] == 10 / 60
    assert params.night_portions()[1] == 15 / 60


def test_calculation_methods():
    params = CalculationMethod.muslim_world_league.calculation_parameters()
    assert params.fajr_angle == 18
    assert params.isha_angle == 17
    assert params.isha_interval == 0
    assert params.method == CalculationMethod.muslim_world_league

    params = CalculationMethod.egyptian.calculation_parameters()
    assert params.fajr_angle == 19.5
    assert params.isha_angle == 17.5
    assert params.isha_interval == 0
    assert params.method == CalculationMethod.egyptian

    params = CalculationMethod.karachi.calculation_parameters()
    assert params.fajr_angle == 18
    assert params.isha_angle == 18
    assert params.isha_interval == 0
    assert params.method == CalculationMethod.karachi

    params = CalculationMethod.umm_al_qura.calculation_parameters()
    assert params.fajr_angle == 18
    assert params.isha_angle == 0
    assert params.isha_interval == 90
    assert params.method == CalculationMethod.umm_al_qura

    params = CalculationMethod.gulf.calculation_parameters()
    assert params.fajr_angle == 19.5
    assert params.isha_angle == 0
    assert params.isha_interval == 90
    assert params.method == CalculationMethod.gulf

    params = CalculationMethod.moonsighting_committee.calculation_parameters()
    assert params.fajr_angle == 18
    assert params.isha_angle == 18
    assert params.isha_interval == 0
    assert params.method == CalculationMethod.moonsighting_committee

    params = CalculationMethod.north_america.calculation_parameters()
    assert params.fajr_angle == 15
    assert params.isha_angle == 15
    assert params.isha_interval == 0
    assert params.method == CalculationMethod.north_america

    params = CalculationMethod.other.calculation_parameters()
    assert params.fajr_angle == 0
    assert params.isha_angle == 0
    assert params.isha_interval == 0
    assert params.method == CalculationMethod.other


def test_prayer_times():
    date = dt.datetime(2015, 7, 12)
    params = CalculationMethod.north_america.calculation_parameters()
    params.madhab = Madhab.hanafi
    coords = Coordinates(35.7750, -78.6336)

    p = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)
    assert p.fajr.strftime('%-I:%M %p') == '8:42 AM'
    assert p.sunrise.strftime('%-I:%M %p') == '10:08 AM'
    assert p.dhuhr.strftime('%-I:%M %p') == '5:21 PM'
    assert p.asr.strftime('%-I:%M %p') == '10:22 PM'
    assert p.maghrib.strftime('%-I:%M %p') == '12:32 AM'
    assert p.isha.strftime('%-I:%M %p') == '1:57 AM'


def test_offsets():
    date = dt.datetime(2015, 12, 1)
    params = CalculationMethod.muslim_world_league.calculation_parameters()
    params.madhab = Madhab.shafi
    coords = Coordinates(35.7750, -78.6336)

    p = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)
    assert p.fajr.strftime('%-I:%M %p') == '10:35 AM'
    assert p.sunrise.strftime('%-I:%M %p') == '12:06 PM'
    assert p.dhuhr.strftime('%-I:%M %p') == '5:05 PM'
    assert p.asr.strftime('%-I:%M %p') == '7:42 PM'
    assert p.maghrib.strftime('%-I:%M %p') == '10:01 PM'
    assert p.isha.strftime('%-I:%M %p') == '11:26 PM'

    params.adjustments.fajr = 10
    params.adjustments.sunrise = 10
    params.adjustments.dhuhr = 10
    params.adjustments.asr = 10
    params.adjustments.maghrib = 10
    params.adjustments.isha = 10

    p = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)
    assert p.fajr.strftime('%-I:%M %p') == '10:45 AM'
    assert p.sunrise.strftime('%-I:%M %p') == '12:16 PM'
    assert p.dhuhr.strftime('%-I:%M %p') == '5:15 PM'
    assert p.asr.strftime('%-I:%M %p') == '7:52 PM'
    assert p.maghrib.strftime('%-I:%M %p') == '10:11 PM'
    assert p.isha.strftime('%-I:%M %p') == '11:36 PM'

    params.adjustments = PrayerAdjustments()

    p = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)
    assert p.fajr.strftime('%-I:%M %p') == '10:35 AM'
    assert p.sunrise.strftime('%-I:%M %p') == '12:06 PM'
    assert p.dhuhr.strftime('%-I:%M %p') == '5:05 PM'
    assert p.asr.strftime('%-I:%M %p') == '7:42 PM'
    assert p.maghrib.strftime('%-I:%M %p') == '10:01 PM'
    assert p.isha.strftime('%-I:%M %p') == '11:26 PM'


def test_moonsighting_method():
    date = dt.datetime(2016, 1, 31)
    params = CalculationMethod.moonsighting_committee.calculation_parameters()
    coords = Coordinates(35.7750, -78.6336)

    p = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)
    assert p.fajr.strftime('%-I:%M %p') == '10:48 AM'
    assert p.sunrise.strftime('%-I:%M %p') == '12:16 PM'
    assert p.dhuhr.strftime('%-I:%M %p') == '5:33 PM'
    assert p.asr.strftime('%-I:%M %p') == '8:20 PM'
    assert p.maghrib.strftime('%-I:%M %p') == '10:43 PM'
    assert p.isha.strftime('%-I:%M %p') == '12:05 AM'
