package de.mephisto.vpin.connectors.vps.model;

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

  public String getImgUrl() {
    return this.oldTable.getTableFiles().get(0).getImgUrl();
  }

  public VpsDiffTypes getDifference() {
    if (diffUrls(oldTable.getAltSoundFiles(), newTable.getAltSoundFiles())) {
      return VpsDiffTypes.altSound;
    }

    if (diffUrls(oldTable.getAltColorFiles(), newTable.getAltColorFiles())) {
      return VpsDiffTypes.altColor;
    }

    if (diffUrls(oldTable.getPovFiles(), newTable.getPovFiles())) {
      return VpsDiffTypes.pov;
    }

    if (diffUrls(oldTable.getRomFiles(), newTable.getRomFiles())) {
      return VpsDiffTypes.rom;
    }

    if (diffUrls(oldTable.getTopperFiles(), newTable.getTopperFiles())) {
      return VpsDiffTypes.topper;
    }

    if (diffUrls(oldTable.getSoundFiles(), newTable.getSoundFiles())) {
      return VpsDiffTypes.sound;
    }

    if (diffUrls(oldTable.getPupPackFiles(), newTable.getPupPackFiles())) {
      return VpsDiffTypes.pupPack;
    }

    if (diffUrls(oldTable.getWheelArtFiles(), newTable.getWheelArtFiles())) {
      return VpsDiffTypes.wheel;
    }

    if (diffUrls(oldTable.getB2sFiles(), newTable.getB2sFiles())) {
      return VpsDiffTypes.b2s;
    }

    VpsDiffTypes diffTypes = diffTables(oldTable.getTableFiles(), newTable.getTableFiles());
    if (diffTypes != null) {
      return diffTypes;
    }

    if (!newTable.getFeatures().stream().filter(item -> !oldTable.getFeatures().contains(item)).collect(Collectors.toList()).isEmpty()) {
      return VpsDiffTypes.feature;
    }
    return null;
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
    if (newUrls != null && oldUrls == null) {
      return true;
    }

    if (newUrls != null) {
      for (VpsAuthoredUrls newUrl : newUrls) {
        if (!oldUrls.contains(newUrl)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return newTable.getDisplayName() + ": changed " + getDifference();
  }
}
