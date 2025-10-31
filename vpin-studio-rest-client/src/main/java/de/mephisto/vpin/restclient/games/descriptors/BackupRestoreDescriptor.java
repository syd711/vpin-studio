package de.mephisto.vpin.restclient.games.descriptors;

public class BackupRestoreDescriptor {
  private int emulatorId = -1;
  private long archiveSourceId;
  private String filename;

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

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }
}
