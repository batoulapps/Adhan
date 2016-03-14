package com.batoulapps.adhan;

/**
 * Madhab for determining how Asr is calculated
 */
public enum Madhab {
  SHAFI,
  HANAFI;

  ShadowLength getShadowLength() {
    switch (this) {
      case SHAFI: {
        return ShadowLength.SINGLE;
      }
      case HANAFI: {
        return ShadowLength.DOUBLE;
      }
      default: {
        throw new IllegalArgumentException("Invalid Madhab");
      }
    }
  }
}
