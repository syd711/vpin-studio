package de.mephisto.vpin.restclient.descriptors;

public class ArchiveInstallDescriptor {
  private int playlistId = -1;
  private long vpaSourceId;
  private String filename;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public long getVpaSourceId() {
    return vpaSourceId;
  }

  public void setArchiveSourceId(long vpaSourceId) {
    this.vpaSourceId = vpaSourceId;
  }

  public int getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(int playlistId) {
    this.playlistId = playlistId;
  }
}
