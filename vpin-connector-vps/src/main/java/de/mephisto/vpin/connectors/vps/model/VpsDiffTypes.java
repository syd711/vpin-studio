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
  tables,
  tableNew,
  tableNewVersion,
  topper,
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
      case tables: {
        return "table added";
      }
      case tableNew: {
        return "new table variant added";
      }
      case tableNewVersion: {
        return "table version updated";
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
