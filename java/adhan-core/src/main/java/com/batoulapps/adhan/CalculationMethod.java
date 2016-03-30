package com.batoulapps.adhan;

public enum CalculationMethod {
  MUSLIM_WORLD_LEAGUE,
  EGYPTIAN,
  KARACHI,
  UMM_AL_QURA,
  GULF,
  MOON_SIGHTING_COMMITTEE,
  NORTH_AMERICA,
  OTHER;

  public CalculationParameters getParameters() {
    switch (this) {
      case MUSLIM_WORLD_LEAGUE: {
        return new CalculationParameters(18.0, 17.0, this);
      }
      case EGYPTIAN: {
        return new CalculationParameters(20.0, 18.0, this);
      }
      case KARACHI: {
        return new CalculationParameters(18.0, 18.0, this);
      }
      case UMM_AL_QURA: {
        return new CalculationParameters(18.0, 90, this);
      }
      case GULF: {
        return new CalculationParameters(19.5, 90, this);
      }
      case MOON_SIGHTING_COMMITTEE: {
        return new CalculationParameters(18.0, 18.0, this);
      }
      case NORTH_AMERICA: {
        return new CalculationParameters(15.0, 15.0, this);
      }
      case OTHER: {
        return new CalculationParameters(0.0, 0.0, this);
      }
      default: {
        throw new IllegalArgumentException("Invalid CalculationMethod");
      }
    }
  }
}
