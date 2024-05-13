package de.mephisto.vpin.restclient.highscores;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;

import java.util.Date;

public class ScoreRepresentation {
  private String playerInitials;
  private PlayerRepresentation player;
  private String score;
  private int position;
  private double numericScore;
  private int gameId;
  private Date createdAt;

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

  @JsonIgnore
  public String getFormattedScore() {
    String scoreString = this.getScore();
    String formattedScore = ScoreFormatUtil.formatScore(scoreString);
    if (!formattedScore.equals("0")) {
      scoreString = formattedScore;
    }
    return scoreString;
  }

  @Override
  public String toString() {
    String name = this.getPlayerInitials();
    if (this.player != null) {
      name = this.player.getName();
    }
    return "#" + this.getPosition() + " " + name + "   " + getFormattedScore();
  }
}
