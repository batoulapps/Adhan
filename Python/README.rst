PrayerTimesLib
--------

To use, simply do::

    >>> from datetime import datetime
    >>> from adhan import CalculationMethod, Coordinates, Madhab, PrayerTimes
    >>> date = datetime(2015, 7, 12)
    >>> params = CalculationMethod.north_america.calculation_parameters()
    >>> params.madhab = Madhab.hanafi
    >>> coords = Coordinates(latitude=35.7750, longitude=-78.6336)
    >>> prayer_times = PrayerTimes(coordinates=coords, date=date, calculation_parameters=params)
    >>> print('Fajr    ', prayer_times.fajr.strftime("%-I:%M %p %z"))
    >>> print('Sunrise ', prayer_times.sunrise.strftime("%-I:%M %p %z"))
    >>> print('Dhuhr   ', prayer_times.dhuhr.strftime("%-I:%M %p %z"))
    >>> print('Asr     ', prayer_times.asr.strftime("%-I:%M %p %z"))
    >>> print('Maghrib ', prayer_times.maghrib.strftime("%-I:%M %p %z"))
    >>> print('Isha    ', prayer_times.isha.strftime("%-I:%M %p %z"))

Note::
    Adhan expects `datetime` objects to be naive. All times are UTC.
