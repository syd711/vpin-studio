package de.mephisto.vpin.restclient.wovp;

public class ScoreSubmit {
  private long latestScore;
  private String playerName;
  private String playerId;
  private String message;
  private String errorMessage;
  private boolean simulate;

  public boolean isSimulate() {
    return simulate;
  }

  public void setSimulate(boolean simulate) {
    this.simulate = simulate;
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

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
