//
//  AdhanObjc.swift
//  Adhan
//
//  Created by Ameir Al-Zoubi on 4/26/16.
//  Copyright © 2016 Batoul Apps. All rights reserved.
//

import Foundation

@objc public enum BAPrayer: Int {
    case fajr = 0
    case sunrise = 1
    case dhuhr = 2
    case asr = 3
    case maghrib = 4
    case isha = 5
    case none = 6
}

@objc open class BAPrayerTimes: NSObject {
    open var fajr: Date?
    open var sunrise: Date?
    open var dhuhr: Date?
    open var asr: Date?
    open var maghrib: Date?
    open var isha: Date?
    
    private let prayerTimes: PrayerTimes?
    
    public init(coordinates: BACoordinates, date: DateComponents, calculationParameters: BACalculationParameters) {
        prayerTimes = PrayerTimes(coordinates: Coordinates(latitude: coordinates.latitude, longitude: coordinates.longitude), date: date, calculationParameters: calculationParameters.calculationParameters())
        if let prayerTimes = prayerTimes {
            self.fajr = prayerTimes.fajr as Date
            self.sunrise = prayerTimes.sunrise as Date
            self.dhuhr = prayerTimes.dhuhr as Date
            self.asr = prayerTimes.asr as Date
            self.maghrib = prayerTimes.maghrib as Date
            self.isha = prayerTimes.isha as Date
        }
        super.init()
    }
    
    open func currentPrayer(_ time: Date?) -> BAPrayer {
        guard let prayerTimes = prayerTimes else {
            return .none
        }
        
        let _time = time ?? Date()
        return BAPrayerForPrayer(prayerTimes.currentPrayer(at: _time))
    }
    
    open func nextPrayer(_ time: Date?) -> BAPrayer {
        guard let prayerTimes = prayerTimes else {
            return .none
        }
        
        let _time = time ?? Date()
        return BAPrayerForPrayer(prayerTimes.nextPrayer(at: _time))
    }
    
    open func timeForPrayer(_ prayer: BAPrayer) -> Date? {
        return prayerTimes?.time(for: prayerForBAPrayer(prayer))
    }
    
    private func prayerForBAPrayer(_ baPrayer: BAPrayer) -> Prayer {
        switch baPrayer {
        case BAPrayer.none:
            return Prayer.none
        case BAPrayer.fajr:
            return Prayer.fajr
        case BAPrayer.sunrise:
            return Prayer.sunrise
        case BAPrayer.dhuhr:
            return Prayer.dhuhr
        case BAPrayer.asr:
            return Prayer.asr
        case BAPrayer.maghrib:
            return Prayer.maghrib
        case BAPrayer.isha:
            return Prayer.isha
        }
    }
    
    private func BAPrayerForPrayer(_ prayer: Prayer) -> BAPrayer {
        switch prayer {
        case Prayer.none:
            return BAPrayer.none
        case Prayer.fajr:
            return BAPrayer.fajr
        case Prayer.sunrise:
            return BAPrayer.sunrise
        case Prayer.dhuhr:
            return BAPrayer.dhuhr
        case Prayer.asr:
            return BAPrayer.asr
        case Prayer.maghrib:
            return BAPrayer.maghrib
        case Prayer.isha:
            return BAPrayer.isha
        }
    }
}

@objc open class BACalculationParameters: NSObject {
    open var method: BACalculationMethod = .other
    open var fajrAngle: Double
    open var ishaAngle: Double
    open var ishaInterval: Int = 0
    open var madhab: BAMadhab = .shafi
    open var highLatitudeRule: BAHighLatitudeRule = .middleOfTheNight
    open var adjustments: BAPrayerAdjustments = BAPrayerAdjustments()
    
    public init(fajrAngle: Double, ishaAngle: Double, ishaInterval: Int) {
        self.fajrAngle = fajrAngle
        self.ishaAngle = ishaAngle
        self.ishaInterval = ishaInterval
        super.init()
    }
    
    public convenience init(method: BACalculationMethod) {
        let params = BACalculationParameters.calculationMethodForBACalculationMethod(method).params
        self.init(fajrAngle: params.fajrAngle, ishaAngle: params.ishaAngle, ishaInterval: params.ishaInterval)
        self.method = method
    }
    
    internal func calculationParameters() -> CalculationParameters {
        var params = CalculationParameters(fajrAngle: self.fajrAngle, ishaAngle: self.ishaAngle)
        params.method = BACalculationParameters.calculationMethodForBACalculationMethod(self.method)
        params.ishaInterval = self.ishaInterval
        params.adjustments = self.adjustments.prayerAdjustments()
        
        switch self.madhab {
        case BAMadhab.shafi:
            params.madhab = Madhab.shafi
        case BAMadhab.hanafi:
            params.madhab = Madhab.hanafi
        }
        
        switch self.highLatitudeRule {
        case BAHighLatitudeRule.middleOfTheNight:
            params.highLatitudeRule = HighLatitudeRule.middleOfTheNight
        case BAHighLatitudeRule.seventhOfTheNight:
            params.highLatitudeRule = HighLatitudeRule.seventhOfTheNight
        case BAHighLatitudeRule.twilightAngle:
            params.highLatitudeRule = HighLatitudeRule.twilightAngle
        }
        
        return params
    }
    
    private static func calculationMethodForBACalculationMethod(_ baMethod: BACalculationMethod) -> CalculationMethod {
        switch baMethod {
        case .algerian: return CalculationMethod.algerian
        case .diyanet: return CalculationMethod.diyanet
        case .egyptianGeneralAuthority: return CalculationMethod.egyptianGeneralAuthority
        case .egyptianGeneralNewAuthority: return CalculationMethod.egyptianGeneralNewAuthority
        case .france15: return CalculationMethod.france15
        case .france18: return CalculationMethod.france18
        case .franceUOIF: return CalculationMethod.franceUOIF
        case .gulf: return CalculationMethod.gulf
        case .JAKIM: return CalculationMethod.JAKIM
        case .jordan: return CalculationMethod.jordan
        case .karachi: return CalculationMethod.karachi
        case .kementerian: return CalculationMethod.kementerian
        case .kuwait: return CalculationMethod.kuwait
        case .moonsightingCommittee: return CalculationMethod.moonsightingCommittee
        case .MUIS: return CalculationMethod.MUIS
        case .muslimWorldLeague: return CalculationMethod.muslimWorldLeague
        case .northAmerica: return CalculationMethod.northAmerica
        case .oman: return CalculationMethod.oman
        case .qatar: return CalculationMethod.qatar
        case .russia: return CalculationMethod.russia
        case .tunisian: return CalculationMethod.tunisian
        case .UAE: return CalculationMethod.UAE
        case .ummAlQura: return CalculationMethod.ummAlQura
        case .ummAlQuraRamadan: return CalculationMethod.ummAlQuraRamadan
        case .other: return CalculationMethod.other
        }
    }
}

@objc public enum BACalculationMethod: Int {
    case algerian
    case diyanet
    case egyptianGeneralAuthority
    case egyptianGeneralNewAuthority
    case france15
    case france18
    case franceUOIF
    case gulf
    case JAKIM
    case jordan
    case karachi
    case kementerian
    case kuwait
    case moonsightingCommittee
    case MUIS
    case muslimWorldLeague
    case northAmerica
    case oman
    case qatar
    case russia
    case tunisian
    case UAE
    case ummAlQura
    case ummAlQuraRamadan
    case other
}

@objc public enum BAMadhab: Int {
    case shafi
    case hanafi
}

@objc public enum BAHighLatitudeRule: Int {
    case middleOfTheNight
    case seventhOfTheNight
    case twilightAngle
}

@objc open class BAPrayerAdjustments: NSObject {
    open var fajr: Int = 0
    open var sunrise: Int = 0
    open var dhuhr: Int = 0
    open var asr: Int = 0
    open var maghrib: Int = 0
    open var isha: Int = 0
    
    public init(fajr: Int = 0, sunrise: Int = 0, dhuhr: Int = 0, asr: Int = 0, maghrib: Int = 0, isha: Int = 0) {
        self.fajr = fajr
        self.sunrise = sunrise
        self.dhuhr = dhuhr
        self.asr = asr
        self.maghrib = maghrib
        self.isha = isha
        super.init()
    }
    
    internal func prayerAdjustments() -> PrayerAdjustments {
        return PrayerAdjustments(fajr: self.fajr, sunrise: self.sunrise, dhuhr: self.dhuhr, asr: self.asr, maghrib: self.maghrib, isha: self.isha)
    }
}

@objc open class BACoordinates: NSObject {
    open var latitude: Double
    open var longitude: Double
    
    public init(latitude: Double, longitude: Double) {
        self.latitude = latitude
        self.longitude = longitude
        super.init()
    }
}

@objc open class BAQibla: NSObject {
    open var direction: Double = 0
    
    public init(coordinates: BACoordinates) {
        let qibla = Qibla(coordinates: Coordinates(latitude: coordinates.latitude, longitude: coordinates.longitude))
        self.direction = qibla.direction
    }
}
