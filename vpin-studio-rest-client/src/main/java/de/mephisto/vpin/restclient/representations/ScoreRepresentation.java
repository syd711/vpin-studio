package de.mephisto.vpin.restclient.representations;

public class ScoreRepresentation {
  private String playerInitials;
  private int playerId;
  private String score;
  private int position;
  private double numericScore;

  public int getPlayerId() {
    return playerId;
  }

  public void setPlayerId(int playerId) {
    this.playerId = playerId;
  }

  public double getNumericScore() {
    return numericScore;
  }

  public void setNumericScore(double numericScore) {
    this.numericScore = numericScore;
  }

  public String getPlayerInitials() {
    return playerInitials;
  }

  public void setPlayerInitials(String playerInitials) {
    this.playerInitials = playerInitials;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }
}
