package de.mephisto.vpin.restclient.pinvol;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PinVolPreferences {
  private List<PinVolTableEntry> tableEntries = new ArrayList<>();
  private int global = 0;
  private int night = 0;
  private int defaultVol = 0;
  private int ssfDbLimit = 10;

  public int getSsfDbLimit() {
    return ssfDbLimit;
  }

  public void setSsfDbLimit(int ssfDbLimit) {
    this.ssfDbLimit = ssfDbLimit;
  }

  public int getGlobal() {
    return global;
  }

  public void setGlobal(int global) {
    this.global = global;
  }

  public int getNight() {
    return night;
  }

  public void setNight(int night) {
    this.night = night;
  }

  public int getDefaultVol() {
    return defaultVol;
  }

  public void setDefaultVol(int defaultVol) {
    this.defaultVol = defaultVol;
  }

  public List<PinVolTableEntry> getTableEntries() {
    return tableEntries;
  }

  public void setTableEntries(List<PinVolTableEntry> tableEntries) {
    this.tableEntries = tableEntries;
  }

  public PinVolTableEntry getSystemVolume() {
    Optional<PinVolTableEntry> system = this.tableEntries.stream().filter(p -> p != null && p.getName() != null && p.getName().equals("System")).findFirst();
    if (system.isPresent()) {
      return system.get();
    }
    PinVolTableEntry entry = new PinVolTableEntry();
    entry.setName("System");
    tableEntries.add(entry);
    return entry;
  }

  public PinVolTableEntry getTableEntry(String fileName, boolean vpxGame, boolean fpGame) {
    String key = getKey(fileName, vpxGame, fpGame);
    return getTableEntry(key);
  }

  public PinVolTableEntry getTableEntry(String key) {
    for (PinVolTableEntry tableEntry : tableEntries) {
      if (tableEntry != null && tableEntry.getName() != null && tableEntry.getName().contains(key)) {
        return tableEntry;
      }
    }
    return null;
  }

  public boolean contains(String key) {
    return getTableEntry(key) != null;
  }

  public static String getKey(String fileName, boolean vpxGame, boolean fpGame) {
    String name = FilenameUtils.getBaseName(String.valueOf(fileName));
    String prefix = "";
    if (vpxGame) {
      prefix = "VP.";
    }
    else if (fpGame) {
      prefix = "FP.";
    }

    return prefix + name;
  }

  public void applyValues(String key, PinVolTableEntry tableVolume) {
    for (PinVolTableEntry tableEntry : tableEntries) {
      if (tableEntry.getName().equals(key)) {
        tableEntry.applyValues(tableVolume);
        return;
      }
    }
  }
}
