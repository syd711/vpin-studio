package de.mephisto.vpin.restclient.system;

import de.mephisto.vpin.restclient.archiving.ArchiveType;

import java.util.List;

public class SystemSummary {
  private ArchiveType archiveType;
  private String systemId;

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public ArchiveType getArchiveType() {
    return archiveType;
  }

  public void setArchiveType(ArchiveType archiveType) {
    this.archiveType = archiveType;
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
