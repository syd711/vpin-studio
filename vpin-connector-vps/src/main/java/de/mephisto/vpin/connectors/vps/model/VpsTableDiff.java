package de.mephisto.vpin.connectors.vps.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class VpsTableDiff {
  private final VpsTable oldTable;
  private final VpsTable newTable;
  private List<VpsDiffTypes> differences;

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

  public String getGameLink() {
    return "https://virtual-pinball-spreadsheet.web.app/game/" + this.getId();
  }

  public Date getLastModified() {
    return new Date(newTable.getUpdatedAt());
  }

  public List<VpsDiffTypes> getDifferences() {
    if(differences == null) {
      differences = new ArrayList<>();

      if (diffUrls(oldTable.getAltSoundFiles(), newTable.getAltSoundFiles())) {
        differences.add(VpsDiffTypes.altSound);
      }

      if (diffUrls(oldTable.getAltColorFiles(), newTable.getAltColorFiles())) {
        differences.add(VpsDiffTypes.altColor);
      }

      if (diffUrls(oldTable.getPovFiles(), newTable.getPovFiles())) {
        differences.add(VpsDiffTypes.pov);
      }

      if (diffUrls(oldTable.getRomFiles(), newTable.getRomFiles())) {
        differences.add(VpsDiffTypes.rom);
      }

      if (diffUrls(oldTable.getTopperFiles(), newTable.getTopperFiles())) {
        differences.add(VpsDiffTypes.topper);
      }

      if (diffUrls(oldTable.getSoundFiles(), newTable.getSoundFiles())) {
        differences.add(VpsDiffTypes.sound);
      }

      if (diffUrls(oldTable.getPupPackFiles(), newTable.getPupPackFiles())) {
        differences.add(VpsDiffTypes.pupPack);
      }

      if (diffUrls(oldTable.getWheelArtFiles(), newTable.getWheelArtFiles())) {
        differences.add(VpsDiffTypes.wheel);
      }

      if (diffUrls(oldTable.getB2sFiles(), newTable.getB2sFiles())) {
        differences.add(VpsDiffTypes.b2s);
      }

      VpsDiffTypes diffTypes = diffTables(oldTable.getTableFiles(), newTable.getTableFiles());
      if (diffTypes != null) {
        differences.add(diffTypes);
      }

//    if (!newTable.getFeatures().stream().filter(item -> !oldTable.getFeatures().contains(item)).collect(Collectors.toList()).isEmpty()) {
//      differences.add(VpsDiffTypes.feature);
//    }
    }

    return differences;
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
    if ((newUrls == null || newUrls.isEmpty()) && (oldUrls == null || oldUrls.isEmpty())) {
      return false;
    }

    if (newUrls != null && oldUrls != null) {
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

  public String getDisplayName() {
    return newTable.getDisplayName();
  }

  @Override
  public String toString() {
    return newTable.getDisplayName() + ": changed " + getDifferences();
  }
}
