package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class VpsDiffer {
  private final VpsTable oldTable;
  private final VpsTable newTable;

  public VpsDiffer(VpsTable newTable, VpsTable oldTable) {
    this.oldTable = oldTable;
    this.newTable = newTable;
  }

  public String getId() {
    return newTable.getId();
  }

  public String getImgUrl() {
    return this.newTable.getTableFiles().get(0).getImgUrl();
  }

  public String getGameLink() {
    return VPS.getVpsTableUrl(this.getId());
  }

  public Date getLastModified() {
    return new Date(newTable.getUpdatedAt());
  }

  public List<VpsDiffTypes> getDifferences() {
    List<VpsDiffTypes> differences = new ArrayList<>();
    if (oldTable == null) {
      differences.add(VpsDiffTypes.tableNewVPX);
      return differences;
    }

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

    if (diffTutorials(oldTable.getTutorialFiles(), newTable.getTutorialFiles())) {
      differences.add(VpsDiffTypes.tutorial);
    }

    VpsDiffTypes diff = diffTables(oldTable.getTableFiles(), newTable.getTableFiles());
    if (diff != null) {
      differences.add(diff);
    }


//    if (!newTable.getFeatures().stream().filter(item -> !oldTable.getFeatures().contains(item)).collect(Collectors.toList()).isEmpty()) {
//      differences.add(VpsDiffTypes.feature);
//    }

    return differences;
  }

  private VpsDiffTypes diffTables(List<VpsTableVersion> oldFiles, List<VpsTableVersion> newFiles) {
    if (oldFiles == null && newFiles == null) {
      return null;
    }

    if (newFiles == null || oldFiles == null) {
      return VpsDiffTypes.tables;
    }

    for (VpsTableVersion newVersionFile : newFiles) {
      Optional<VpsTableVersion> versionInOtherList = oldFiles.stream().filter(t -> t.getId().equals(newVersionFile.getId())).findFirst();
      if (versionInOtherList.isPresent()) {
        VpsTableVersion version = versionInOtherList.get();
        if (version.getVersion() == null && newVersionFile.getVersion() == null) {
          continue;
        }

        if (!String.valueOf(version.getVersion()).equals(newVersionFile.getVersion())) {
          if(newVersionFile.getTableFormat() != null && newVersionFile.getTableFormat().equals("FP")) {
            return VpsDiffTypes.tableNewVersionFP;
          }
          return VpsDiffTypes.tableNewVersionVPX;
        }
      } else {
        if(newVersionFile.getTableFormat() != null && newVersionFile.getTableFormat().equals("FP")) {
          return VpsDiffTypes.tableNewFP;
        }
        return VpsDiffTypes.tableNewVPX;
      }
    }

    return null;
  }

  private boolean diffTutorials(List<VpsTutorialUrls> oldUrls, List<VpsTutorialUrls> newUrls) {
    if ((newUrls == null || newUrls.isEmpty()) && (oldUrls == null || oldUrls.isEmpty())) {
      return false;
    }

    if (newUrls != null && !newUrls.isEmpty() && (oldUrls == null || oldUrls.isEmpty())) {
      return true;
    }

    if (newUrls == null || newUrls.isEmpty()) {
      return true;
    }

    for (VpsTutorialUrls newUrl : newUrls) {
      if (!oldUrls.contains(newUrl)) {
        return true;
      }
    }

    for (VpsTutorialUrls oldUrl : oldUrls) {
      if (!newUrls.contains(oldUrl)) {
        return true;
      }
    }
    return false;
  }

  private boolean diffUrls(List<? extends VpsAuthoredUrls> oldUrls, List<? extends VpsAuthoredUrls> newUrls) {
    if ((newUrls == null || newUrls.isEmpty()) && (oldUrls == null || oldUrls.isEmpty())) {
      return false;
    }

    if (newUrls != null && !newUrls.isEmpty() && (oldUrls == null || oldUrls.isEmpty())) {
      return true;
    }

    if (newUrls == null || newUrls.isEmpty()) {
      return true;
    }


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
