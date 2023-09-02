package de.mephisto.vpin.restclient.altsound;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AltSound2SampleType {
  music, callout, sfx, solo, overlay;

  public static List<String> toStringValues() {
    return Arrays.stream(values()).map(e -> e.name().toUpperCase()).collect(Collectors.toList());
  }
}
