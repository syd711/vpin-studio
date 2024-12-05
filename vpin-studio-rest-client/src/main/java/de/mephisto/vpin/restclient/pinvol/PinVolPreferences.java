package de.mephisto.vpin.restclient.pinvol;

import org.apache.commons.io.FilenameUtils;

import java.util.List;
import java.util.Optional;

public class PinVolPreferences {
  private List<PinVolTableEntry> tableEntries;

  public List<PinVolTableEntry> getTableEntries() {
    return tableEntries;
  }

  public void setTableEntries(List<PinVolTableEntry> tableEntries) {
    this.tableEntries = tableEntries;
  }

  public PinVolTableEntry getSystemVolume() {
    Optional<PinVolTableEntry> system = this.tableEntries.stream().filter(p -> p.getName().equals("System")).findFirst();
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
    for (PinVolTableEntry tableEntry : tableEntries) {
      if (tableEntry.getName().contains(key)) {
        return tableEntry;
      }
    }
    return null;
  }

  public boolean contains(String key) {
    for (PinVolTableEntry tableEntry : tableEntries) {
      if (tableEntry.getName().equals(key)) {
        return true;
      }
    }
    return false;
  }

  public static String getKey(String fileName, boolean vpxGame, boolean fpGame) {
    String name = FilenameUtils.getBaseName(fileName);
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
