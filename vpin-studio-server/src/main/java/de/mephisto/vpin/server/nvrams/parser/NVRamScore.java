package de.mephisto.vpin.server.nvrams.parser;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representing a single entry from a nvram mapping file.
 */
public class NVRamScore extends NVRamObject {

  // A label describing this descriptor. 
  @JsonProperty("label")
  private String label;

  // An optional, abbreviated label for use when space is limited (like in a game launcher on a DMD). 
  @JsonProperty("short_label")
  private String shortLabel;

  @JsonProperty("initials")
  private NVRamMapping initials;
  @JsonProperty("score")
  private NVRamMapping score;
  @JsonProperty("timestamp")
  private NVRamMapping timestamp;
  @JsonProperty("counter")
  private NVRamMapping counter;
  @JsonProperty("nth time")
  private NVRamMapping nthtime;

  //---------------------------------------- Getters only for JSONProperties

  public String getLabel() {
    return label;
  }

  public String getShortLabel() {
    return shortLabel;
  }

  public NVRamMapping getInitials() {
    return initials;
  }

  public NVRamMapping getScore() {
    return score;
  }

  public NVRamMapping getTimestamp() {
    return timestamp;
  }

  //------------------------------------------------

  public List<Integer> offsets() {

    List<Integer> o = new ArrayList<>();
    if (initials != null) {
      o.addAll(initials.offsets());
    }
    if (score != null) {
      o.addAll(score.offsets());
    }
    if (timestamp != null) {
      o.addAll(timestamp.offsets());
    }
    return o;
  }

  public String formatHighScore(NVRamMap mapJson, SparseMemory memory, Locale locale) {
    List<String> elements = new ArrayList<>();
    if (initials != null) {
      String formatted = initials.formatEntry(mapJson, memory, locale);
      if (formatted != null) elements.add(formatted);
    }
    if (score != null) {
      String formatted = score.formatEntry(mapJson, memory, locale);
      if (formatted != null) elements.add(formatted);
    }
    if (timestamp != null) {
      String formatted = timestamp.formatEntry(mapJson, memory, locale);
      if (formatted != null) elements.add(formatted);
    }
    return elements.isEmpty() ? null : String.join(" ", elements);
  }

  public String formatLabel(boolean useShortLabel) {
    String lbl = StringUtils.defaultString(label, "?");
    if (lbl.startsWith("_")) lbl = null;
    if (useShortLabel) {
      if (shortLabel != null) lbl = shortLabel;
    }
    return lbl;
  }
}
