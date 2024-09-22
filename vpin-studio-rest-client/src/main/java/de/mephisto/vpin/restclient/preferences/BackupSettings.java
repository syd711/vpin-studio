package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class BackupSettings extends JsonSettings {

  private String vpbmInternalHostId;
  private String vpbmExternalHostId1;
  private String vpbmExternalHostId2;
  private String vpbmExternalHostId3;

  public String getVpbmInternalHostId() {
    return vpbmInternalHostId;
  }

  public void setVpbmInternalHostId(String vpbmInternalHostId) {
    this.vpbmInternalHostId = vpbmInternalHostId;
  }

  public String getVpbmExternalHostId1() {
    return vpbmExternalHostId1;
  }

  public void setVpbmExternalHostId1(String vpbmExternalHostId1) {
    this.vpbmExternalHostId1 = vpbmExternalHostId1;
  }

  public String getVpbmExternalHostId2() {
    return vpbmExternalHostId2;
  }

  public void setVpbmExternalHostId2(String vpbmExternalHostId2) {
    this.vpbmExternalHostId2 = vpbmExternalHostId2;
  }

  public String getVpbmExternalHostId3() {
    return vpbmExternalHostId3;
  }

  public void setVpbmExternalHostId3(String vpbmExternalHostId3) {
    this.vpbmExternalHostId3 = vpbmExternalHostId3;
  }
}
