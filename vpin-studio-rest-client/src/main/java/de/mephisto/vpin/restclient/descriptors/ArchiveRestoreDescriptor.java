package de.mephisto.vpin.restclient.descriptors;

public class ArchiveRestoreDescriptor {
  private int playlistId = -1;
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

  public int getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(int playlistId) {
    this.playlistId = playlistId;
  }
}
