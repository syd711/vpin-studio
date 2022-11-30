package de.mephisto.vpin.restclient.representations;

public class ScoreRepresentation {
  private String playerInitials;
  private PlayerRepresentation player;
  private String score;
  private int position;
  private double numericScore;
  private int gameId;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public PlayerRepresentation getPlayer() {
    return player;
  }

  public void setPlayer(PlayerRepresentation player) {
    this.player = player;
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
