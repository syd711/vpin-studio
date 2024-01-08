package de.mephisto.vpin.restclient.games.descriptors;

public class ArchiveCopyToRepositoryDescriptor {
  private long archiveSourceId;
  private String filename;
  private boolean overwrite;

  public boolean isOverwrite() {
    return overwrite;
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public long getArchiveSourceId() {
    return archiveSourceId;
  }

  public void setArchiveSourceId(long sourceId) {
    this.archiveSourceId = sourceId;
  }
}
