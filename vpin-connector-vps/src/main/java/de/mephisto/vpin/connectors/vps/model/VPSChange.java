package de.mephisto.vpin.connectors.vps.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class VPSChange {
  private final static Logger LOG = LoggerFactory.getLogger(VPSChange.class);

  private VPSEntity changedEntity;
  private VpsDiffTypes diffType;
  private String id;

  public VPSChange() {

  }

  public VPSChange(VPSEntity changedItem, VpsDiffTypes diffType) {
    this.changedEntity = changedItem;
    this.diffType = diffType;
    this.id = changedItem.getId();
  }

  public String toString(VpsTable tableById) {
    if (tableById == null) {
      return "Table null";
    }

    if (diffType.equals(VpsDiffTypes.tableNewVPX)) {
      return diffType.toString();
    }

    switch (diffType) {
      case altColor: {
        if (tableById.getAltColorFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getAltColorFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.altColor + ":\n" + first.get();
          }
        }
        break;
      }
      case altSound: {
        if (tableById.getAltSoundFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getAltSoundFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.altSound + ":\n" + first.get();
          }
        }
        break;
      }
      case b2s: {
        if(tableById.getB2sFiles() != null) {
          Optional<VpsBackglassFile> first = tableById.getB2sFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.b2s + ":\n" + first.get();
          }
        }
        break;
      }
      case pov: {
        if (tableById.getPovFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getPovFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.pov + ":\n" + first.get();
          }
        }
        break;
      }
      case rom: {
        if (tableById.getRomFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getRomFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.rom + ":\n" + first.get();
          }
        }
        break;
      }
      case sound: {
        if (tableById.getSoundFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getSoundFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.sound + ":\n" + first.get();
          }
        }
        break;
      }
      case pupPack: {
        if (tableById.getPupPackFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getPupPackFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.pupPack + ":\n" + first.get();
          }
        }
        break;
      }
      case wheel: {
        if (tableById.getWheelArtFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getWheelArtFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.wheel + ":\n" + first.get();
          }
        }
        break;
      }
      case tutorial: {
        List<VpsTutorialUrls> tutorialFiles = tableById.getTutorialFiles();
        if (tutorialFiles != null) {
          Optional<VpsTutorialUrls> first = tutorialFiles.stream().filter(f -> f != null && f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.tutorial + ":\n" + first.get();
          }
        }
        break;
      }
      case tableNewVPX: {
        return VpsDiffTypes.tableNewVPX + ":\n- " + tableById.toString();
      }
      case tableNewVersionVPX: {
        VpsTableVersion version = tableById.getTableVersionById(this.getId());
        if (version != null) {
          return VpsDiffTypes.tableNewVersionVPX + ":\n- " + version;
        }
        return null;
      }
      case topper: {
        if (tableById.getTopperFiles() != null) {
          Optional<VpsAuthoredUrls> first = tableById.getTopperFiles().stream().filter(f -> f.getId() != null && f.getId().equals(this.getId())).findFirst();
          if (first.isPresent()) {
            return VpsDiffTypes.topper + ":\n" + first.get();
          }
        }
        break;
      }
    }
    LOG.warn("No toString representation found for " + diffType + " and table " + tableById);
    return null;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @JsonIgnore
  public VPSEntity getChangedEntity() {
    return changedEntity;
  }

  public void setChangedEntity(VPSEntity changedEntity) {
    this.changedEntity = changedEntity;
  }

  public VpsDiffTypes getDiffType() {
    return diffType;
  }

  public void setDiffType(VpsDiffTypes diffType) {
    this.diffType = diffType;
  }

  @Override
  public String toString() {
    return "VPSChange{" +
        "changedEntity=" + changedEntity +
        ", diffType=" + diffType +
        '}';
  }
}
