package de.mephisto.vpin.restclient.mania;

public class ManiaAccountRepresentation {
  private String uuid;
  private String cabinetId;
  private String initials;
  private String displayName;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getCabinetId() {
    return cabinetId;
  }

  public void setCabinetId(String cabinetId) {
    this.cabinetId = cabinetId;
  }

  public String getInitials() {
    return initials;
  }

  public void setInitials(String initials) {
    this.initials = initials;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
