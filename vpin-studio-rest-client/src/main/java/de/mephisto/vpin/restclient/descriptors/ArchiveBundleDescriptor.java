package de.mephisto.vpin.restclient.descriptors;

import java.util.ArrayList;
import java.util.List;

public class ArchiveBundleDescriptor {
  private long archiveSourceId;
  private List<String> archiveNames = new ArrayList<>();

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
