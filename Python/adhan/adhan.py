from enum import Enum, IntEnum, unique
import math
import datetime as dt


# Madhab for determining how Asr is calculated
@unique
class Madhab(Enum):
    shafi = 1
    hanafi = 2

    def shadow_length(self):
        return {
            Madhab.shafi: ShadowLength.single,
            Madhab.hanafi: ShadowLength.double
        }[self]


@unique
class ShadowLength(IntEnum):
    single = 1
    double = 2


# Rule for approximating Fajr and Isha at high latitudes
@unique
class HighLatitudeRule(Enum):
    middle_of_the_night = 1
    seventh_of_the_night = 2
    twilight_angle = 3


class Coordinates:
    def __init__(self, latitude, longitude):
        if not (-90 <= latitude <= 90):
            raise ValueError
        if not (-180 <= longitude <= 180):
            raise ValueError
        self.latitude = latitude
        self.longitude = longitude


# Adjustment value for prayer times, in minutes
class PrayerAdjustments:
    def __init__(self, fajr=0, sunrise=0, dhuhr=0, asr=0, maghrib=0, isha=0):
        self.fajr = fajr
        self.sunrise = sunrise
        self.dhuhr = dhuhr
        self.asr = asr
        self.maghrib = maghrib
        self.isha = isha


# All customizable parameters for calculating prayer times
class CalculationParameters:
    def __init__(self, fajr_angle=None, isha_angle=None, isha_interval=None, method=None):
        self.method = method or CalculationMethod.other
        self.fajr_angle = fajr_angle or 0
        self.isha_angle = isha_angle or 0
        self.isha_interval = isha_interval or 0
        self.madhab = Madhab.shafi
        self.high_latitude_rule = HighLatitudeRule.middle_of_the_night
        self.adjustments = PrayerAdjustments()

    # returns tuple (fajr_portion, isha_portion)
    def night_portions(self):
        return {
            HighLatitudeRule.middle_of_the_night: (1 / 2, 1 / 2),
            HighLatitudeRule.seventh_of_the_night: (1 / 7, 1 / 7),
            HighLatitudeRule.twilight_angle: (self.fajr_angle / 60, self.isha_angle / 60)
        }[self.high_latitude_rule]


# Preset calculation parameters
@unique
class CalculationMethod(Enum):
    muslim_world_league = 1
    egyptian = 2
    karachi = 3
    umm_al_qura = 4
    gulf = 5
    moonsighting_committee = 6
    north_america = 7
    other = 8

    def calculation_parameters(self):
        if self == CalculationMethod.muslim_world_league:
            return CalculationParameters(fajr_angle=18, isha_angle=17, method=self)
        elif self == CalculationMethod.egyptian:
            return CalculationParameters(fajr_angle=20, isha_angle=18, method=self)
        elif self == CalculationMethod.karachi:
            return CalculationParameters(fajr_angle=18, isha_angle=18, method=self)
        elif self == CalculationMethod.umm_al_qura:
            return CalculationParameters(fajr_angle=18, isha_interval=90, method=self)
        elif self == CalculationMethod.gulf:
            return CalculationParameters(fajr_angle=19.5, isha_interval=90, method=self)
        elif self == CalculationMethod.moonsighting_committee:
            return CalculationParameters(fajr_angle=18, isha_angle=18, method=self)
        elif self == CalculationMethod.north_america:
            return CalculationParameters(fajr_angle=15, isha_angle=15, method=self)
        else:
            return CalculationParameters(fajr_angle=0, isha_angle=0, method=self)


# Prayer times for a location and date using the given calculation parameters.
# All prayer times are in UTC
class PrayerTimes:
    def __init__(self, coordinates, date, calculation_parameters):
        self.fajr = None
        self.sunrise = None
        self.dhuhr = None
        self.asr = None
        self.maghrib = None
        self.isha = None

        is_moonsighting = calculation_parameters.method == CalculationMethod.moonsighting_committee

        solar_time = SolarTime(date, coordinates)

        sunrise = date_with_hours(date, solar_time.sunrise)
        dhuhr = date_with_hours(date, solar_time.transit)
        asr = date_with_hours(date, solar_time.afternoon(calculation_parameters.madhab.shadow_length()))
        maghrib = date_with_hours(date, solar_time.sunset)
        isha = None

        if not all((sunrise, dhuhr, asr, maghrib)):
            raise Exception('fail', 'whale')
            return None

        tomorrow_sunrise = sunrise + dt.timedelta(days=1)
        if tomorrow_sunrise is None:
            raise Exception('fail', 'whale')
            return None

        night_duration = tomorrow_sunrise - maghrib

        fajr = self.__calculate_fajr(coordinates, date, calculation_parameters, solar_time, sunrise, night_duration)
        isha = self.__calculate_isha(coordinates, date, calculation_parameters, solar_time, maghrib, night_duration)

        if fajr is None:
            raise Exception('Unable to calculate fajr')
        if isha is None:
            raise Exception('Unable to calculate isha')

        # Moonsighting Committee requires 5 minutes for the sun
        # to pass the zenith and dhuhr to enter
        # Default behavior waits 1 minute for the sun to pass
        # the zenith and dhuhr to enter
        dhuhr_offset = 300 if is_moonsighting else 60

        # Moonsighting Committee adds 3 minutes to sunset time
        # to account for light refraction
        maghrib_offset = 180 if is_moonsighting else 0

        self.fajr = rounded_minute(fajr + dt.timedelta(minutes=calculation_parameters.adjustments.fajr))
        self.sunrise = rounded_minute(sunrise + dt.timedelta(minutes=calculation_parameters.adjustments.sunrise))
        self.dhuhr = rounded_minute(dhuhr + dt.timedelta(minutes=calculation_parameters.adjustments.dhuhr + dhuhr_offset))
        self.asr = rounded_minute(asr + dt.timedelta(minutes=calculation_parameters.adjustments.asr))
        self.maghrib = rounded_minute(maghrib + dt.timedelta(minutes=calculation_parameters.adjustments.maghrib + maghrib_offset))
        self.isha = rounded_minute(isha + dt.timedelta(minutes=calculation_parameters.adjustments.isha))

    @classmethod
    def __calculate_fajr(cls, coordinates, date, calculation_parameters, solar_time, sunrise, night_duration):
        fajr = date_with_hours(date, solar_time.hour_angle(-calculation_parameters.fajr_angle, False))

        # fajr check against safe value
        safe_fajr = None
        if calculation_parameters.method == CalculationMethod.moonsighting_committee:
            if coordinates.latitude < 55:
                safe_fajr = cls.season_adjusted_fajr(coordinates.latitude, date, sunrise)
            else:
                night_fration = night_duration / 7
                safe_fajr = sunrise - night_fration
        else:
            fajr_portion, isha_portion = calculation_parameters.night_portions()
            night_fration = night_duration * fajr_portion
            safe_fajr = sunrise - night_fration

        if fajr is None or fajr < safe_fajr:
            fajr = safe_fajr

        return fajr

    @classmethod
    def __calculate_isha(cls, coordinates, date, calculation_parameters, solar_time, sunset, night_duration):
        # isha calcuation w/ check against safe value
        if calculation_parameters.isha_interval > 0:
            isha = sunset + dt.timedelta(minutes=calculation_parameters.isha_interval)
        else:
            temp_isha = date_with_hours(date, solar_time.hour_angle(-calculation_parameters.isha_angle, True))
            if temp_isha is not None:
                isha = temp_isha

            safe_isha = None
            if calculation_parameters.method == CalculationMethod.moonsighting_committee:
                if coordinates.latitude < 55:
                    safe_isha = cls.season_adjusted_isha(coordinates.latitude, date, sunset)
                else:
                    night_fration = night_duration / 7
                    safe_isha = sunset + night_fration
            else:
                fajr_portion, isha_portion = calculation_parameters.night_portions()
                night_fration = night_duration * isha_portion
                safe_isha = sunset + night_fration

            if isha is None or isha > safe_isha:
                isha = safe_isha

        return isha

    @classmethod
    def season_adjusted_fajr(cls, latitude, date, sunrise):
        a = 75 + 28.65 / 55.0 * math.fabs(latitude)
        b = 75 + 19.44 / 55.0 * math.fabs(latitude)
        c = 75 + 32.74 / 55.0 * math.fabs(latitude)
        d = 75 + 48.10 / 55.0 * math.fabs(latitude)

        dyy = cls.days_since_solstice(latitude, date)
        if dyy < 91:
            adjustment = a + (b - a) / 91.0 * dyy
        elif dyy < 137:
            adjustment = b + (c - b) / 46.0 * (dyy - 91)
        elif dyy < 183:
            adjustment = c + (d - c) / 46.0 * (dyy - 137)
        elif dyy < 229:
            adjustment = d + (c - d) / 46.0 * (dyy - 183)
        elif dyy < 275:
            adjustment = c + (b - c) / 46.0 * (dyy - 229)
        else:
            adjustment = b + (a - b) / 91.0 * (dyy - 275)

        return sunrise + dt.timedelta(math.floor(adjustment) * -60)

    @classmethod
    def season_adjusted_isha(cls, latitude, date, sunrise):
        a = 75 + 28.65 / 55.0 * math.fabs(latitude)
        b = 75 + 19.44 / 55.0 * math.fabs(latitude)
        c = 75 + 32.74 / 55.0 * math.fabs(latitude)
        d = 75 + 48.10 / 55.0 * math.fabs(latitude)

        dyy = cls.days_since_solstice(latitude, date)
        if dyy < 91:
            adjustment = a + (b - a) / 91.0 * dyy
        elif dyy < 137:
            adjustment = b + (c - b) / 46.0 * (dyy - 91)
        elif dyy < 183:
            adjustment = c + (d - c) / 46.0 * (dyy - 137)
        elif dyy < 229:
            adjustment = d + (c - d) / 46.0 * (dyy - 183)
        elif dyy < 275:
            adjustment = c + (b - c) / 46.0 * (dyy - 229)
        else:
            adjustment = b + (a - b) / 91.0 * (dyy - 275)

        return sunrise + dt.timedelta(math.ceil(adjustment) * 60)

    @classmethod
    def days_since_solstice(cls, latitude, date):
        year = date.year
        day_of_year = date.timetuple().tm_yday
        days_since_solstice = 0
        northern_offset = 10
        southern_offset = 173 if is_leap_year(year) else 172
        days_in_year = 366 if is_leap_year(year) else 365

        if latitude >= 0:
            days_since_solstice = day_of_year + northern_offset
            if days_since_solstice >= days_in_year:
                days_since_solstice = days_since_solstice - days_in_year
        else:
            days_since_solstice = day_of_year - southern_offset
            if days_since_solstice < 0:
                days_since_solstice = days_since_solstice + days_in_year

        return days_since_solstice


def normalize(angle, bound):
    normalized = angle - (bound * (math.floor(angle / bound)))
    return normalized if normalized >= 0 else normalized + bound


def unwind_angle_360(angle):
    return normalize(angle, 360)


def unwind_angle_180(angle):
    unwound = unwind_angle_360(angle)
    return unwound if unwound <= 180 else unwound - 360


def rounded_minute(date):
    return dt.datetime(date.year, date.month, date.day, date.hour, int(date.minute + round(date.second / 60.0)))


def date_with_hours(date, hours):
    if (math.isnan(hours)):
        return None
    return dt.datetime(date.year, date.month, date.day) + dt.timedelta(hours=hours)


# Julian Day Number for a given date
# Equation from Astronomical Algorithms p60
def julian_day(date):
    Y = date.year if date.month > 2 else date.year - 1
    M = date.month if date.month > 2 else date.month + 12
    D = date.day + date.hour / 24.0

    A = int(Y / 100)
    B = 2 - A + int(A / 4)

    i = int(365.25 * (Y + 4716))
    j = int(30.6001 * (M + 1))

    return i + j + D + B - 1524.5


# Julian century from the epoch
# Equation from Astronomical Algorithms p163
def julian_century(julian_day):
    return (julian_day - 2451545.0) / 36525


def is_leap_year(year):
    if year % 4 != 0:
        return False
    if year % 100 == 0 and year % 400 != 0:
        return False
    return True


# The geometric mean longitude of the sun in degrees
# Equation from Astronomical Algorithms p163
def mean_solar_longitude(julian_century):
    T = julian_century

    i = 280.4664567
    j = 36000.76983 * T
    k = 0.0003032 * math.pow(T, 2)
    L0 = i + j + k
    return unwind_angle_360(L0)


# The geometric mean longitude of the moon in degrees
# Equation from Astronomical Algorithms p144
def mean_lunar_longitude(julian_century):
    T = julian_century

    i = 218.3165
    j = 481267.8813 + T
    Lp = i + j
    return unwind_angle_360(Lp)


# The apparent longitude of the Sun, referred to the true equinox of the date.
# Equation from Astronomical Algorithms p164
def apparent_solar_longitude(julian_century, mean_longitude):
    T = julian_century
    L0 = mean_longitude

    M = mean_solar_anomaly(T)
    longitude = L0 + solar_equation_of_the_center(T, M)
    Ω = 125.04 - (1934.136 * T)
    λ = longitude - 0.00569 - (0.00478 * math.sin(math.radians(Ω)))
    return unwind_angle_360(λ)


# Equation from Astronomical Algorithms p144
def ascending_lunar_node_longitude(julian_century):
    T = julian_century

    i = 125.04452
    j = 1934.136261 * T
    k = 0.0020708 * math.pow(T, 2)
    l = math.pow(T, 3) / 450000
    Ω = i - j + k + l
    return unwind_angle_360(Ω)


# The mean anomaly of the sun.
# Equation from Astronomical Algorithms p163
def mean_solar_anomaly(julian_century):
    T = julian_century

    i = 357.52911
    j = 35999.05029 * T
    k = 0.0001537 * math.pow(T, 2)
    M = i + j - k
    return unwind_angle_360(M)


# The Sun's equation of the center in degrees.
# Equation from Astronomical Algorithms p164
def solar_equation_of_the_center(julian_century, mean_anomaly):
    T = julian_century
    M = mean_anomaly

    Mrad = math.radians(M)
    i = (1.914602 - (0.004817 * T) - (0.000014 * math.pow(T, 2))) * math.sin(Mrad)
    j = (0.019993 - (0.000101 * T)) * math.sin(2 * Mrad)
    k = 0.000289 * math.sin(3 * Mrad)
    return i + j + k


# The mean obliquity of the ecliptic, formula adopted by the International Astronomical Union.
# Equation from Astronomical Algorithms p147
def mean_obliquity_of_the_ecliptic(julian_century):
    T = julian_century

    i = 23.439291
    j = 0.013004167 * T
    k = 0.0000001639 * math.pow(T, 2)
    l = 0.0000005036 * math.pow(T, 3)
    return i - j - k + l


# The mean obliquity of the ecliptic, corrected for calculating the apparent position of the sun, in degrees.
# Equation from Astronomical Algorithms p165
def apparent_obliquity_of_the_ecliptic(julian_century, mean_obliquity_of_the_ecliptic):
    T = julian_century

    O = 125.04 - (1934.136 * T)
    return mean_obliquity_of_the_ecliptic + (0.00256 * math.cos(math.radians(O)))


# Mean sidereal time, the hour angle of the vernal equinox, in degrees.
# Equation from Astronomical Algorithms p165
def mean_sidereal_time(julian_century):
    T = julian_century
    JD = (T * 36525) + 2451545.0

    i = 280.46061837
    j = 360.98564736629 * (JD - 2451545)
    k = 0.000387933 * math.pow(T, 2)
    l = math.pow(T, 3) / 38710000
    θ = i + j + k - l
    return unwind_angle_360(θ)


# Equation from Astronomical Algorithms p144
def nutation_in_longitude(solar_longitude, lunar_longitude, ascending_node):
    L0 = solar_longitude
    Lp = lunar_longitude
    Ω = ascending_node

    i = (-17.2 / 3600) * math.sin(math.radians(Ω))
    j = (1.32 / 3600) * math.sin(2 * math.radians(L0))
    k = (0.23 / 3600) * math.sin(2 * math.radians(Lp))
    l = (0.21 / 3600) * math.sin(2 * math.radians(Ω))
    return i - j - k + l


# Equation from Astronomical Algorithms p144
def nutation_in_obliquity(solar_longitude, lunar_longitude, ascending_node):
    L0 = solar_longitude
    Lp = lunar_longitude
    Ω = ascending_node

    i = (9.2 / 3600) * math.cos(math.radians(Ω))
    j = (0.57 / 3600) * math.cos(2 * math.radians(L0))
    k = (0.10 / 3600) * math.cos(2 * math.radians(Lp))
    l = (0.09 / 3600) * math.cos(2 * math.radians(Ω))
    return i + j + k - l


# Equation from Astronomical Algorithms p93
def altitude_of_celestial_body(observer_latitude, declination, local_hour_angle):
    φ = observer_latitude
    δ = declination
    H = local_hour_angle

    i = math.sin(math.radians(φ)) * math.sin(math.radians(δ))
    j = math.cos(math.radians(φ)) * math.cos(math.radians(δ)) * math.cos(math.radians(H))
    return math.degrees(math.asin(i + j))


# Equation from page Astronomical Algorithms p102
def approximate_transit(longitude, sidereal_time, right_ascension):
    L = longitude
    Θ0 = sidereal_time
    α2 = right_ascension

    Lw = L * -1
    return normalize(((α2 + Lw - Θ0) / 360), 1)


# The time at which the sun is at its highest point in the sky (in universal time)
# Equation from page Astronomical Algorithms p102
def corrected_transit(approximate_transit, longitude, sidereal_time, right_ascension, previous_right_ascension, next_right_ascension):
    m0 = approximate_transit
    L = longitude
    Θ0 = sidereal_time
    α1 = previous_right_ascension
    α2 = right_ascension
    α3 = next_right_ascension

    Lw = L * -1
    θ = unwind_angle_360(Θ0 + (360.985647 * m0))
    α = interpolate(α2, α1, α3, m0)
    H = (θ - Lw - α)
    Δm = H / -360 if (H >= -180 and H <= 180) else 0
    return (m0 + Δm) * 24


# Equation from page Astronomical Algorithms p102
def correctedHourAngle(approximate_transit, angle, coordinates, after_transit, sidereal_time, right_ascension, previous_right_ascension, next_right_ascension, declination, previous_declination, next_declination):
    m0 = approximate_transit
    h0 = angle
    Θ0 = sidereal_time
    α1 = previous_right_ascension
    α2 = right_ascension
    α3 = next_right_ascension
    δ1 = previous_declination
    δ2 = declination
    δ3 = next_declination

    Lw = coordinates.longitude * -1
    i = math.sin(math.radians(h0)) - (math.sin(math.radians(coordinates.latitude)) * math.sin(math.radians(δ2)))
    j = math.cos(math.radians(coordinates.latitude)) * math.cos(math.radians(δ2))
    H0 = math.radians(math.acos(i / j))
    m = m0 + (H0 / 360) if after_transit else m0 - (H0 / 360)
    θ = unwind_angle_360(Θ0 + (360.985647 * m))
    α = interpolate(α2, α1, α3, m)
    δ = interpolate(δ2, δ1, δ3, m)
    H = (θ - Lw - α)
    h = altitude_of_celestial_body(coordinates.latitude, δ, H)
    k = h - h0
    l = 360 * math.cos(math.radians(δ)) * math.cos(math.radians(coordinates.latitude)) * math.sin(math.radians(H))
    Δm = k / l
    return (m + Δm) * 24


# Interpolation of a value given equidistant previous and next values and a factor equal to the fraction of the interpolated
# point's time over the time between values.
# Equation from Astronomical Algorithms p24
def interpolate(value, previous_value, next_value, factor):
    y1 = previous_value
    y2 = value
    y3 = next_value
    n = factor

    a = y2 - y1
    b = y3 - y2
    c = b - a
    return y2 + ((n / 2) * (a + b + (n * c)))


class SolarCoordinates:
    def __init__(self, julian_day):
        T = julian_century(julian_day)
        L0 = mean_solar_longitude(T)
        Lp = mean_lunar_longitude(T)
        Ω = ascending_lunar_node_longitude(T)
        λ = math.radians(apparent_solar_longitude(T, L0))
        θ0 = mean_sidereal_time(T)
        ΔΨ = nutation_in_longitude(L0, Lp, Ω)
        Δε = nutation_in_obliquity(L0, Lp, Ω)
        ε0 = mean_obliquity_of_the_ecliptic(T)
        εapp = math.radians(apparent_obliquity_of_the_ecliptic(T, ε0))

        # Equation from Astronomical Algorithms p165
        self.declination = math.degrees(math.asin(math.sin(εapp) * math.sin(λ)))

        # Equation from Astronomical Algorithms p165
        self.right_ascension = unwind_angle_360(math.degrees(math.atan2(math.cos(εapp) * math.sin(λ), math.cos(λ))))

        # Equation from Astronomical Algorithms p88
        self.apparent_sidereal_time = θ0 + ((ΔΨ * 3600) * math.cos(math.radians(ε0 + Δε)) / 3600)


class SolarTime:
    def __init__(self, date, coordinates):
        # Calculations need to occur at 0h0m UTC
        date = dt.datetime(date.year, date.month, date.day)

        next_date = date + dt.timedelta(1)
        previous_date = date - dt.timedelta(-1)

        solar = SolarCoordinates(julian_day(date))
        next_solar = SolarCoordinates(julian_day(next_date))
        previous_solar = SolarCoordinates(julian_day(previous_date))

        m0 = approximate_transit(coordinates.longitude, solar.apparent_sidereal_time, solar.right_ascension)
        solar_altitude = -50.0 / 60.0

        self.date = date
        self.observer = coordinates
        self.solar = solar
        self.next_solar = next_solar
        self.previous_solar = previous_solar
        self.approximate_transit = m0
        self.transit = corrected_transit(m0, coordinates.longitude, solar.apparent_sidereal_time, solar.right_ascension, previous_solar.right_ascension, next_solar.right_ascension)
        self.sunrise = correctedHourAngle(m0, solar_altitude, coordinates, False, solar.apparent_sidereal_time, solar.right_ascension, previous_solar.right_ascension, next_solar.right_ascension, solar.declination, previous_solar.declination, next_solar.declination)
        self.sunset = correctedHourAngle(m0, solar_altitude, coordinates, True, solar.apparent_sidereal_time, solar.right_ascension, previous_solar.right_ascension, next_solar.right_ascension, solar.declination, previous_solar.declination, next_solar.declination)

    def hour_angle(self, angle, after_transit):
        return correctedHourAngle(self.approximate_transit, angle, self.observer, after_transit, self.solar.apparent_sidereal_time, self.solar.right_ascension, self.previous_solar.right_ascension, self.next_solar.right_ascension, self.solar.declination, self.previous_solar.declination, self.next_solar.declination)

    def afternoon(self, shadow_length):
        tangent = math.fabs(self.observer.latitude - self.solar.declination)
        inverse = shadow_length + math.tan(math.radians(tangent))
        angle = math.degrees(math.atan(1.0 / inverse))
        return self.hour_angle(angle, True)
