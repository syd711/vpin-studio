package de.mephisto.vpin.connectors.vps.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
