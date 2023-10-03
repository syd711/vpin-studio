package de.mephisto.vpin.restclient.system;

import de.mephisto.vpin.restclient.archiving.ArchiveType;

import java.util.List;

public class SystemSummary {
  private String pinupSystemDirectory;
  private ArchiveType archiveType;

  public ArchiveType getArchiveType() {
    return archiveType;
  }

  public void setArchiveType(ArchiveType archiveType) {
    this.archiveType = archiveType;
  }

  private List<ScreenInfo> screenInfos;

  public List<ScreenInfo> getScreenInfos() {
    return screenInfos;
  }

  public void setScreenInfos(List<ScreenInfo> screenInfos) {
    this.screenInfos = screenInfos;
  }

  public String getPinupSystemDirectory() {
    return pinupSystemDirectory;
  }

  public void setPinupSystemDirectory(String pinupSystemDirectory) {
    this.pinupSystemDirectory = pinupSystemDirectory;
  }

  public ScreenInfo getMainScreenInfo() {
    for (ScreenInfo screenInfo : screenInfos) {
      if (screenInfo.isPrimary()) {
        return screenInfo;
      }
    }
    return screenInfos.get(0);
  }

  public ScreenInfo getScreenInfo(int id) {
    for (ScreenInfo screenInfo : screenInfos) {
      if (screenInfo.getId() == id) {
        return screenInfo;
      }
    }
    return null;
  }
}
