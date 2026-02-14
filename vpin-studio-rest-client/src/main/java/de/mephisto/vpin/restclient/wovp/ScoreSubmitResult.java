package de.mephisto.vpin.restclient.wovp;

public class ScoreSubmitResult {
  private long latestScore;
  private String playerName;
  private String errorMessage;

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public long getLatestScore() {
    return latestScore;
  }

  public void setLatestScore(long latestScore) {
    this.latestScore = latestScore;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
