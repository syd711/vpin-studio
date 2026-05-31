package de.mephisto.vpin.connectors.vps.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

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

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonCreator
    public static VpsDiffTypes fromString(String key) {
        for (VpsDiffTypes type : values()) {
            if (type.name().equalsIgnoreCase(key) || type.toString().equalsIgnoreCase(key)) {
                return type;
            }
        }
        LOG.warn("Unknown VpsDiffTypes key found: '" + key + "'");
        return null;
    }

  @Override
  public String toString() {
      return switch (this) {
          case altColor -> "ALT Color";
          case altSound -> "ALT Sound";
          case b2s -> "Backglass";
          case pov -> "POV";
          case rom -> "ROM";
          case sound -> "Sound";
          case pupPack -> "PUP pack";
          case wheel -> "Wheel Icon";
          case tutorial -> "Tutorial Added";
          case tableNewVPX -> "New Table Added";
          case tableNewVersionVPX -> "New Table Added";
          case tableVersionUpdate -> "Table Version Updated";
          case tableNewVersion -> "New Table Version Added";
          case topper -> "Topper";
          case feature -> "Feature";
          default -> "Invalid component type: " + this.name();
      };
  }
}
