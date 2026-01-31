package de.mephisto.vpin.restclient.system;

import de.mephisto.vpin.restclient.backups.BackupType;

import java.util.List;

public class SystemSummary {
  private BackupType backupType;
  private List<MonitorInfo> monitorInfos;

  public BackupType getBackupType() {
    return backupType;
  }

  public void setBackupType(BackupType backupType) {
    this.backupType = backupType;
  }

  public List<MonitorInfo> getMonitorInfos() {
    return monitorInfos;
  }

  public void setScreenInfos(List<MonitorInfo> monitorInfos) {
    this.monitorInfos = monitorInfos;
  }

  public MonitorInfo getPrimaryMonitor() {
    return getMonitorInfo(-1);
  }

  public MonitorInfo getMonitorInfo(int id) {
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
