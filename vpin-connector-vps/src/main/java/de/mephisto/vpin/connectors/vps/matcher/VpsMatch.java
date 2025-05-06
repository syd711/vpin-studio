package de.mephisto.vpin.connectors.vps.matcher;

public class VpsMatch {

  private int gameId = -1;

  private String extTableId;

  private String extTableVersionId;

  private String version;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getExtTableId() {
    return extTableId;
  }

  public void setExtTableId(String extTableId) {
    this.extTableId = extTableId;
  }

  public String getExtTableVersionId() {
    return extTableVersionId;
  }

  public void setExtTableVersionId(String extVersionId) {
    this.extTableVersionId = extVersionId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
