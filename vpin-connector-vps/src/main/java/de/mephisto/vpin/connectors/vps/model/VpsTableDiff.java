package de.mephisto.vpin.connectors.vps.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VpsTableDiff {
  private final VpsTable oldTable;
  private final VpsTable newTable;

  public VpsTableDiff(VpsTable newTable, VpsTable oldTable) {
    this.oldTable = oldTable;
    this.newTable = newTable;
  }

  public String getId() {
    return oldTable.getId();
  }

  public String getImgUrl() {
    return this.oldTable.getTableFiles().get(0).getImgUrl();
  }

  public List<VpsDiffTypes> getDifferences() {
    List<VpsDiffTypes> result = new ArrayList<>();
    if (diffUrls(oldTable.getAltSoundFiles(), newTable.getAltSoundFiles())) {
      result.add(VpsDiffTypes.altSound);
    }

    if (diffUrls(oldTable.getAltColorFiles(), newTable.getAltColorFiles())) {
      result.add(VpsDiffTypes.altColor);
    }

    if (diffUrls(oldTable.getPovFiles(), newTable.getPovFiles())) {
      result.add(VpsDiffTypes.pov);
    }

    if (diffUrls(oldTable.getRomFiles(), newTable.getRomFiles())) {
      result.add(VpsDiffTypes.rom);
    }

    if (diffUrls(oldTable.getTopperFiles(), newTable.getTopperFiles())) {
      result.add(VpsDiffTypes.topper);
    }

    if (diffUrls(oldTable.getSoundFiles(), newTable.getSoundFiles())) {
      result.add(VpsDiffTypes.sound);
    }

    if (diffUrls(oldTable.getPupPackFiles(), newTable.getPupPackFiles())) {
      result.add(VpsDiffTypes.pupPack);
    }

    if (diffUrls(oldTable.getWheelArtFiles(), newTable.getWheelArtFiles())) {
      result.add(VpsDiffTypes.wheel);
    }

    if (diffUrls(oldTable.getB2sFiles(), newTable.getB2sFiles())) {
      result.add(VpsDiffTypes.b2s);
    }

    VpsDiffTypes diffTypes = diffTables(oldTable.getTableFiles(), newTable.getTableFiles());
    if (diffTypes != null) {
      result.add(diffTypes);
    }

//    if (!newTable.getFeatures().stream().filter(item -> !oldTable.getFeatures().contains(item)).collect(Collectors.toList()).isEmpty()) {
//      result.add(VpsDiffTypes.feature);
//    }
    return result;
  }

  private VpsDiffTypes diffTables(List<VpsTableFile> oldFiles, List<VpsTableFile> newFiles) {
    if (oldFiles != null && newFiles == null) {
      return VpsDiffTypes.tables;
    }

    for (VpsTableFile newTable : newFiles) {
      Optional<VpsTableFile> first = oldFiles.stream().filter(t -> t.getId().equals(newTable.getId())).findFirst();
      if (first.isPresent()) {
        VpsTableFile vpsTableFile = first.get();
        if (!String.valueOf(vpsTableFile.getVersion()).equals(newTable.getVersion())) {
          return VpsDiffTypes.tableNewVersion;
        }
      }
      else {
        return VpsDiffTypes.tableNew;
      }
    }

    return null;
  }

  private boolean diffUrls(List<? extends VpsAuthoredUrls> oldUrls, List<? extends VpsAuthoredUrls> newUrls) {
    if (newUrls != null && oldUrls == null && !newUrls.isEmpty()) {
      return true;
    }

    if (newUrls != null) {
      for (VpsAuthoredUrls newUrl : newUrls) {
        if (!oldUrls.contains(newUrl)) {
          return true;
        }
      }
      for (VpsAuthoredUrls oldUrl : oldUrls) {
        if (!newUrls.contains(oldUrl)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return newTable.getDisplayName() + ": changed " + getDifferences();
  }
}
