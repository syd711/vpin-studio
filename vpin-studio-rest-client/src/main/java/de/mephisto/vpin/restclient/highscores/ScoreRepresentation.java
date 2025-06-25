package de.mephisto.vpin.restclient.highscores;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;

import java.util.Date;

public class ScoreRepresentation {
  private String playerInitials;
  private PlayerRepresentation player;
  private boolean external;
  private String rawScore;
  private int position;
  private long score;
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

  public String getPlayerInitials() {
    return playerInitials;
  }

  public void setPlayerInitials(String playerInitials) {
    this.playerInitials = playerInitials;
  }

  public boolean isExternal() {
    return external;
  }

  public void setExternal(boolean external) {
    this.external = external;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public long getScore() {
    return score;
  }

  public void setScore(long score) {
    this.score = score;
  }

  public String getRawScore() {
    return rawScore;
  }

  public void setRawScore(String rawScore) {
    this.rawScore = rawScore;
  }

  @JsonIgnore
  public String getFormattedScore() {
    String formattedScore = ScoreFormatUtil.formatScore(this.getScore());
    //TODO was it really needed  
    // Maybe because if NumberFormatException is caught in formatScore, it returns "0" but that should never happen 
    //if (!formattedScore.equals("0")) {
      return formattedScore;
    //}
    //return rawScore;
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
