package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.players.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Score {
  private String playerInitials = "???";
  private int gameId;
  private Player player;
  private String score;
  private double numericScore;
  private int position;
  private Date createdAt;

  public Score(Date createdAt, int gameId, String playerInitials, Player player, String score, double numericScore, int position) {
    this.createdAt = createdAt;
    this.gameId = gameId;
    this.player = player;
    this.score = score;
    this.numericScore = numericScore;
    this.position = position;
    if (!StringUtils.isEmpty(playerInitials)) {
      this.playerInitials = playerInitials;
    }
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getPlayerInitials() {
    while (playerInitials.length() < 3) {
      playerInitials += " ";
    }
    return playerInitials;
  }

  public void setPlayerInitials(String playerInitials) {
    if (!StringUtils.isEmpty(playerInitials.trim())) {
      this.playerInitials = playerInitials;
    }
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void setNumericScore(double numericScore) {
    this.numericScore = numericScore;
  }

  public double getNumericScore() {
    return numericScore;
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

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Score)) {
      return false;
    }

    Score score = (Score) obj;
    return score.getPlayerInitials().equalsIgnoreCase(this.getPlayerInitials())
        && score.getPosition() == this.getPosition()
        && score.getNumericScore() == this.getNumericScore();
  }

  public void setScore(String score) {
    this.score = score;
  }

  public boolean matches(Score newScore) {
    return this.playerInitials != null && this.playerInitials.equals(newScore.getPlayerInitials())
        && this.score != null && this.numericScore == newScore.getNumericScore();

  }

  @Override
  public String toString() {
    String name = this.getPlayerInitials();
    if(this.player != null) {
      name = this.player.getName();
    }
    return "#" + this.getPosition() + " " + name + "   " + this.getScore();
  }
}
