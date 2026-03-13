package de.mephisto.vpin.restclient.games.descriptors;

public class VPXZExportDescriptor {
  private int gameId;
  private long sourceId;
  private String vpxStandaloneFile;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getVpxStandaloneFile() {
    return vpxStandaloneFile;
  }

  public void setVpxStandaloneFile(String vpxStandaloneFile) {
    this.vpxStandaloneFile = vpxStandaloneFile;
  }

  public long getSourceId() {
    return sourceId;
  }

  public void setSourceId(long sourceId) {
    this.sourceId = sourceId;
  }
}
