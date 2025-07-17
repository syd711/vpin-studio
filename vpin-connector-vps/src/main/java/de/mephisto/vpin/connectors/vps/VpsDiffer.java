package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class VpsDiffer {
  private final static Logger LOG = LoggerFactory.getLogger(VpsDiffer.class);
  private final VpsTable oldTable;
  private final VpsTable newTable;
  private final List<String> authorDenyList;

  public VpsDiffer(VpsTable newTable, VpsTable oldTable, List<String> authorDenyList) {
    this.oldTable = oldTable;
    this.newTable = newTable;
    this.authorDenyList = authorDenyList;
  }

  public String getId() {
    return newTable.getId();
  }

  public Date getLastModified() {
    return new Date(newTable.getUpdatedAt());
  }

  public VPSChanges getTableChanges() {
    return getChanges(true);
  }

  public VPSChanges getChanges() {
    return getChanges(true);
  }

  private VPSChanges getChanges(boolean skipNewEvents) {
    VPSChanges changes = new VPSChanges();
    List<VPSChange> differences = changes.getChanges();

    if (oldTable == null) {
      if (!skipNewEvents) {
        differences.add(new VPSChange(newTable, VpsDiffTypes.tableNewVPX));
      }
      return changes;
    }

    diffUrls(oldTable.getAltSoundFiles(), newTable.getAltSoundFiles(), VpsDiffTypes.altSound, differences);
    diffUrls(oldTable.getAltColorFiles(), newTable.getAltColorFiles(), VpsDiffTypes.altColor, differences);
    diffUrls(oldTable.getPovFiles(), newTable.getPovFiles(), VpsDiffTypes.pov, differences);
    diffUrls(oldTable.getRomFiles(), newTable.getRomFiles(), VpsDiffTypes.rom, differences);
    diffUrls(oldTable.getTopperFiles(), newTable.getTopperFiles(), VpsDiffTypes.topper, differences);
    diffUrls(oldTable.getSoundFiles(), newTable.getSoundFiles(), VpsDiffTypes.sound, differences);
    diffUrls(oldTable.getPupPackFiles(), newTable.getPupPackFiles(), VpsDiffTypes.pupPack, differences);
    diffUrls(oldTable.getWheelArtFiles(), newTable.getWheelArtFiles(), VpsDiffTypes.wheel, differences);
    diffUrls(oldTable.getB2sFiles(), newTable.getB2sFiles(), VpsDiffTypes.b2s, differences);
    diffTutorials(oldTable.getTutorialFiles(), newTable.getTutorialFiles(), differences);
    diffTables(oldTable.getTableFiles(), newTable.getTableFiles(), differences, skipNewEvents);

    return changes;
  }

  private void diffTables
      (List<VpsTableVersion> oldFiles, List<VpsTableVersion> newFiles, List<VPSChange> differences, boolean skipNewEvents) {
    if ((newFiles == null || newFiles.isEmpty()) && (oldFiles == null || oldFiles.isEmpty())) {
      return;
    }

    if (newFiles != null && !newFiles.isEmpty() && (oldFiles == null || oldFiles.isEmpty())) {
      return;
    }

    if (newFiles == null || newFiles.isEmpty()) {
      return;
    }

    for (VpsTableVersion newVersionFile : newFiles) {
      if (isIgnored(newVersionFile.getAuthors())) {
        LOG.info("Ignored table version \"{}\" of table \"{}\", is on deny list: \"{}\"", newVersionFile, newTable.getName(), authorDenyList);
        return;
      }

      Optional<VpsTableVersion> versionInOtherList = oldFiles.stream().filter(t -> t.getId() != null && t.getId().equals(newVersionFile.getId())).findFirst();
      if (versionInOtherList.isPresent()) {
        VpsTableVersion version = versionInOtherList.get();
        if (version.getVersion() == null && newVersionFile.getVersion() == null) {
          continue;
        }

        String version1 = String.valueOf(version.getVersion());
        String version2 = newVersionFile.getVersion();
        if (!version1.equals(version2) && version.getId().equals(newVersionFile.getId())) {
          differences.add(new VPSChange(newVersionFile, VpsDiffTypes.tableVersionUpdate));
        }
        else if (!version.getId().equals(newVersionFile.getId()) && !skipNewEvents) {
          differences.add(new VPSChange(newVersionFile, VpsDiffTypes.tableNewVersion));
        }
      }
    }
  }

  private void diffTutorials
      (List<VpsTutorialUrls> oldUrls, List<VpsTutorialUrls> newUrls, List<VPSChange> differences) {
    if ((newUrls == null || newUrls.isEmpty()) && (oldUrls == null || oldUrls.isEmpty())) {
      return;
    }

    if (newUrls != null && !newUrls.isEmpty() && (oldUrls == null || oldUrls.isEmpty())) {
      return;
    }

    if (newUrls == null || newUrls.isEmpty()) {
      return;
    }

    for (VpsTutorialUrls newUrl : newUrls) {
      if (isIgnored(newUrl.getAuthors())) {
        LOG.info("Ignored {}, is on deny list\"{}\"", newUrl, authorDenyList);
        return;
      }
      if (!oldUrls.contains(newUrl)) {
        differences.add(new VPSChange(newUrl, VpsDiffTypes.tutorial));
        break;
      }
    }
  }

  private void diffUrls(List<? extends VpsAuthoredUrls> oldUrls, List<? extends
      VpsAuthoredUrls> newUrls, VpsDiffTypes diffType, List<VPSChange> differences) {
    if ((newUrls == null || newUrls.isEmpty()) && (oldUrls == null || oldUrls.isEmpty())) {
      return;
    }

    if (newUrls != null && !newUrls.isEmpty() && (oldUrls == null || oldUrls.isEmpty())) {
      return;
    }

    if (newUrls == null || newUrls.isEmpty()) {
      return;
    }

    for (VpsAuthoredUrls newUrl : newUrls) {
      if (isIgnored(newUrl.getAuthors())) {
        LOG.info("Ignored asset {} with URL {}, is on deny list\"{}\"", diffType, newUrl, authorDenyList);
        return;
      }

      if (newUrl.containsUpdatedVersion(oldUrls) || !newUrl.isContainedIn(oldUrls)) {
        differences.add(new VPSChange(newUrl, diffType));
        return;
      }
    }
  }

  private boolean isIgnored(List<String> authors) {
    String authorString = String.join(" ", authors).toLowerCase();
    for (String s : authorDenyList) {
      if (authorString.contains(s.toLowerCase())) {
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
    return newTable.getDisplayName() + ": changed " + getChanges();
  }
}
