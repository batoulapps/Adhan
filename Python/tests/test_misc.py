import datetime as dt
import pytest
from adhan_batoulapps.adhan import Coordinates, UTC


def test_coordinates():
    coords = Coordinates(0, 0)
    assert coords.latitude == 0 and coords.longitude == 0

    delhi = Coordinates(28.6139, -77.2090)
    assert delhi.latitude == 28.6139 and delhi.longitude == -77.2090

    london = Coordinates(51.5074, 0.1278)
    assert london.latitude == 51.5074 and london.longitude == 0.1278


def test_coordinates_exceptions():
    with pytest.raises(ValueError):
        Coordinates(-91, 0)
    with pytest.raises(ValueError):
        Coordinates(91, 0)
    with pytest.raises(ValueError):
        Coordinates(0, -181)
    with pytest.raises(ValueError):
        Coordinates(0, 181)


def test_utc():
    utc = UTC()
    date = dt.datetime(2001, 1, 1, 12, tzinfo=utc)
    assert date.tzinfo.utcoffset(date) == dt.timedelta(0)
    assert date.tzinfo.dst(date) == dt.timedelta(0)
    assert date.tzinfo.tzname(date) == "UTC"
