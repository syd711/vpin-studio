package de.mephisto.vpin.commons;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

public class OrbitalPins {
  private final static List<String> roms = Arrays.asList("leprechaun", "STLE", "hpgof", "diablo", "pizzatime");

  public static boolean isOrbitalPin(@NonNull String rom) {
    return roms.contains(rom);
  }
}
