package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class BackupSettings extends JsonSettings {

  private String vpbmInternalHostId;
  private String vpbmExternalHostId1;
  private String vpbmExternalHostId2;

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

  @Override
  public String getSettingsName() {
    return PreferenceNames.BACKUP_SETTINGS;
  }
}
