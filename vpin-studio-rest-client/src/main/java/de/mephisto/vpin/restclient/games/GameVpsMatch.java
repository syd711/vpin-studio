package de.mephisto.vpin.restclient.games;

public class GameVpsMatch {

  private int gameId = -1;

  private String extTableId;

  private String extTableVersionId;

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
}