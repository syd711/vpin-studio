package de.mephisto.vpin.restclient.games.descriptors;

import java.util.ArrayList;
import java.util.List;

public class ArchiveBundleDescriptor {
  private long archiveSourceId;
  private String exportHostId1;
  private String exportHostId2;
  private String exportHostId3;
  private List<String> archiveNames = new ArrayList<>();

  public String getExportHostId1() {
    return exportHostId1;
  }

  public void setExportHostId1(String exportHostId1) {
    this.exportHostId1 = exportHostId1;
  }

  public String getExportHostId2() {
    return exportHostId2;
  }

  public void setExportHostId2(String exportHostId2) {
    this.exportHostId2 = exportHostId2;
  }

  public String getExportHostId3() {
    return exportHostId3;
  }

  public void setExportHostId3(String exportHostId3) {
    this.exportHostId3 = exportHostId3;
  }

  public long getArchiveSourceId() {
    return archiveSourceId;
  }

  public void setArchiveSourceId(long archiveSourceId) {
    this.archiveSourceId = archiveSourceId;
  }

  public List<String> getArchiveNames() {
    return archiveNames;
  }

  public void setArchiveNames(List<String> archiveNames) {
    this.archiveNames = archiveNames;
  }
}
