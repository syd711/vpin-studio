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
  tableNewVersion,
  tableNewVersionVPX,
  tableVersionUpdate,
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
        return "New Table Added";
      }
      case tableVersionUpdate: {
        return "Table Version Updated";
      }
      case tableNewVersion: {
        return "New Table Version Added";
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
