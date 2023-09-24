package de.mephisto.vpin.restclient.tables.descriptors;

import java.util.ArrayList;
import java.util.List;

public class ArchiveBundleDescriptor {
  private long archiveSourceId;
  private String exportHostId;
  private List<String> archiveNames = new ArrayList<>();

  public String getExportHostId() {
    return exportHostId;
  }

  public void setExportHostId(String exportHostId) {
    this.exportHostId = exportHostId;
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
