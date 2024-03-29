package de.mephisto.vpin.connectors.vps.model;

public enum VpsDiffTypes {
  altColor,
  altSound,
  b2s,
  pov,
  rom,
  sound,
  pupPack,
  wheel,
  tableNewVPX,
  tableNewVersionVPX,
  topper,
  tutorial,
  feature,
  ;

  @Override
  public String toString() {
    switch (this) {
      case altColor: {
        return "ALT Color";
      }
      case altSound: {
        return "ALT Sound";
      }
      case b2s: {
        return "Backglass";
      }
      case pov: {
        return "POV";
      }
      case rom: {
        return "ROM";
      }
      case sound: {
        return "Sound";
      }
      case pupPack: {
        return "PUP pack";
      }
      case wheel: {
        return "Wheel Icon";
      }
      case tutorial: {
        return "Tutorial Added";
      }
      case tableNewVPX: {
        return "New Table Added";
      }
      case tableNewVersionVPX: {
        return "Table Version Updated";
      }
      case topper: {
        return "Topper";
      }
      default: {
        throw new UnsupportedOperationException("Invalid component type " + this);
      }
    }
  }
}
