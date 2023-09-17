package de.mephisto.vpin.restclient.altsound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AltSound2SampleType {
  music, callout, sfx, solo, overlay;

  public static List<String> toStringValues() {
    return Arrays.stream(values()).map(e -> e.name().toUpperCase()).collect(Collectors.toList());
  }

  public static List<String> toProfilesStringValues() {
    List<String> values = new ArrayList<>(toStringValues());
    values.remove(solo.name().toUpperCase());
    values.remove(music.name().toUpperCase());
    return values;
  }
}
