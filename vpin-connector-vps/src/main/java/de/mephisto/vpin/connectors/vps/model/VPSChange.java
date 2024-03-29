package de.mephisto.vpin.connectors.vps.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.connectors.vps.VPS;

import java.util.Optional;

public class VPSChange {

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

  public String toString(String tableId) {
    if (diffType.equals(VpsDiffTypes.tableNewVPX)) {
      return diffType.toString();
    }
    VpsTable tableById = VPS.getInstance().getTableById(tableId);
    switch (diffType) {
      case altColor: {
        Optional<VpsAuthoredUrls> first = tableById.getAltColorFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.altColor + "\n" + first.get();
        }
        break;
      }
      case altSound: {
        Optional<VpsAuthoredUrls> first = tableById.getAltSoundFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.altSound + "\n" + first.get();
        }
        break;
      }
      case b2s: {
        Optional<VpsBackglassFile> first = tableById.getB2sFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.b2s + "\n" + first.get();
        }
        break;
      }
      case pov: {
        Optional<VpsAuthoredUrls> first = tableById.getPovFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.pov + "\n" + first.get();
        }
        break;
      }
      case rom: {
        Optional<VpsAuthoredUrls> first = tableById.getRomFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.rom + "\n" + first.get();
        }
        break;
      }
      case sound: {
        Optional<VpsAuthoredUrls> first = tableById.getSoundFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.sound + "\n" + first.get();
        }
        break;
      }
      case pupPack: {
        Optional<VpsAuthoredUrls> first = tableById.getPupPackFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.pupPack + "\n" + first.get();
        }
        break;
      }
      case wheel: {
        Optional<VpsAuthoredUrls> first = tableById.getWheelArtFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.wheel + "\n" + first.get();
        }
        break;
      }
      case tutorial: {
        Optional<VpsTutorialUrls> first = tableById.getTutorialFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.tutorial + "\n" + first.get();
        }
        break;
      }
      case tableNewVPX: {
        return VpsDiffTypes.tableNewVPX + "\n" + this.changedEntity.toString();
      }
      case tableNewVersionVPX: {
        return VpsDiffTypes.tableNewVersionVPX + "\n" + this.changedEntity.toString();
      }
      case topper: {
        Optional<VpsAuthoredUrls> first = tableById.getTopperFiles().stream().filter(f -> f.getId().equals(this.getId())).findFirst();
        if (first.isPresent()) {
          return VpsDiffTypes.topper + "\n" + first.get();
        }
        break;
      }
    }
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
