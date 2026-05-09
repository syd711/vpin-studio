package de.mephisto.vpin.restclient.highscores;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;

import java.time.OffsetDateTime;

public class ScoreRepresentation {

  private String playerInitials;
  private PlayerRepresentation player;
  private boolean external;
  private String rawScore;
  private int position;
  private long score;
  private int gameId;
  private OffsetDateTime createdAt;
  private String label;       // optional label for titled scores, high-scores, buy-in scores....
  private String suffix;      // optional suffix

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public boolean hasPlayer() {
    return player != null;
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

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
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
