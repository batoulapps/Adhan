import os
import fnmatch
import json
import pytz
import datetime as dt
import pytest
from pyadhan.adhan import CalculationMethod, Madhab, HighLatitudeRule, Coordinates, PrayerTimes


def parse_params(data):
    try:
        method = {
            'MuslimWorldLeague': CalculationMethod.muslim_world_league,
            'Egyptian': CalculationMethod.egyptian,
            'Karachi': CalculationMethod.karachi,
            'UmmAlQura': CalculationMethod.umm_al_qura,
            'Gulf': CalculationMethod.gulf,
            'MoonsightingCommittee': CalculationMethod.moonsighting_committee,
            'NorthAmerica': CalculationMethod.north_america,
            'MuslimWorldLeague': CalculationMethod.other
        }[data.get('method')]
    except KeyError:
        method = CalculationMethod.other
    params = method.calculation_parameters()

    try:
        params.madhab = {
            'Shafi': Madhab.shafi,
            'Hanafi': Madhab.hanafi
        }[data.get('madhab')]
    except KeyError:
        pass

    try:
        params.high_latitude_rule = {
            'MiddleOfTheNight': HighLatitudeRule.middle_of_the_night,
            'SeventhOfTheNight': HighLatitudeRule.seventh_of_the_night,
            'TwilightAngle': HighLatitudeRule.twilight_angle
        }[data.get('highLatitudeRule')]
    except KeyError:
        pass

    return params


def generate_tests():
    path = os.path.dirname(os.path.realpath(__file__))
    tests = list()
    for file in os.listdir(path):
        if fnmatch.fnmatch(file, '*.json'):
            with open(os.path.join(path, file)) as json_data:
                data = json.load(json_data)
                params = data.get('params')
                latitude = params.get('latitude')
                longitude = params.get('longitude')
                timezone = pytz.timezone(params.get('timezone'))

                coordinates = Coordinates(latitude, longitude)
                calculation_parameters = parse_params(params)

                def testify(time):
                    date = dt.datetime.strptime(time.get('date'), '%Y-%m-%d')
                    prayer_times = PrayerTimes(coordinates=coordinates, date=date, calculation_parameters=calculation_parameters)
                    return {
                        'timezone': timezone,
                        'prayer_times': prayer_times,
                        'times': time
                    }

                tests = tests + list(map(testify, data.get('times')))
    return tests


@pytest.mark.parametrize("test", generate_tests())
def test_times(test):
    timezone = test.get('timezone')
    prayer_times = test.get('prayer_times')
    times = test.get('times')
    time_format = '%-I:%M %p'
    assert prayer_times.fajr.astimezone(timezone).strftime(time_format) == times.get('fajr')
    assert prayer_times.sunrise.astimezone(timezone).strftime(time_format) == times.get('sunrise')
    assert prayer_times.dhuhr.astimezone(timezone).strftime(time_format) == times.get('dhuhr')
    assert prayer_times.asr.astimezone(timezone).strftime(time_format) == times.get('asr')
    assert prayer_times.maghrib.astimezone(timezone).strftime(time_format) == times.get('maghrib')
    assert prayer_times.isha.astimezone(timezone).strftime(time_format) == times.get('isha')
