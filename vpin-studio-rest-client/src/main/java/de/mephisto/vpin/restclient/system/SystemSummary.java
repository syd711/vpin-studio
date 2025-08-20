package de.mephisto.vpin.restclient.system;

import de.mephisto.vpin.restclient.backups.BackupType;

import java.util.List;

public class SystemSummary {
  private BackupType backupType;
  private String systemId;

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public BackupType getBackupType() {
    return backupType;
  }

  public void setBackupType(BackupType backupType) {
    this.backupType = backupType;
  }

  private List<MonitorInfo> monitorInfos;

  public List<MonitorInfo> getScreenInfos() {
    return monitorInfos;
  }

  public void setScreenInfos(List<MonitorInfo> monitorInfos) {
    this.monitorInfos = monitorInfos;
  }

  public MonitorInfo getPrimaryScreen() {
    return getScreenInfo(-1);
  }

  public MonitorInfo getScreenInfo(int id) {
    for (MonitorInfo monitorInfo : monitorInfos) {
      if (id == -1 && monitorInfo.isPrimary()) {
        return monitorInfo;
      }
      else if (monitorInfo.getId() == id) {
        return monitorInfo;
      }
    }
    return null;
  }
}
