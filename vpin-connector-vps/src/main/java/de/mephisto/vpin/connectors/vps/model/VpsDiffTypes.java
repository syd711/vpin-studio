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
      case tables: {
        return "Table added";
      }
      case tutorial: {
        return "Tutorial added";
      }
      case tableNew: {
        return "New table added";
      }
      case tableNewVersion: {
        return "Table version updated";
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
