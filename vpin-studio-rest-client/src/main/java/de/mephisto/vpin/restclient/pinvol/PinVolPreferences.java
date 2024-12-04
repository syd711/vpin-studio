package de.mephisto.vpin.restclient.pinvol;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

public class PinVolPreferences {
  private List<PinVolTableEntry> tableEntries;

  public List<PinVolTableEntry> getTableEntries() {
    return tableEntries;
  }

  public void setTableEntries(List<PinVolTableEntry> tableEntries) {
    this.tableEntries = tableEntries;
  }

  public PinVolTableEntry getSystemVolume() {
    return this.tableEntries.stream().filter(p -> p.getName().equals("System")).findFirst().get();
  }

  public PinVolTableEntry getTableEntry(GameRepresentation game) {
    String fileName = game.getGameFileName();
    String name = FilenameUtils.getBaseName(fileName);
    String prefix = "";
    if (game.isVpxGame()) {
      prefix = "VP.";
    }
    else if (game.isFpGame()) {
      prefix = "FP.";
    }
    for (PinVolTableEntry tableEntry : tableEntries) {
      if (tableEntry.getName().contains(prefix + name)) {
        return tableEntry;
      }
    }
    return null;
  }
}
