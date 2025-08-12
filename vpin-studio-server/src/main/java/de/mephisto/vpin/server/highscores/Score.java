package de.mephisto.vpin.server.highscores;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.connectors.mania.model.DeniedScore;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.players.Player;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Locale;

public class Score {
  private String playerInitials = "???";
  private int gameId;
  private Player player;
  private long score;
  /** Should not be used anymore, contains the raw score before parsing - consider removing */
  private String rawScore;
  private int position;
  private Date createdAt;
  private boolean external;

  public Score(Date createdAt, int gameId, String playerInitials, Player player, String rawScore, long score, int position) {
    this.createdAt = createdAt;
    this.gameId = gameId;
    this.player = player;
    this.score = score;
    this.rawScore = rawScore;
    this.position = position;
    if (!StringUtils.isEmpty(playerInitials)) {
      this.playerInitials = playerInitials;
    }
  }

  @JsonIgnore
  public boolean isExternal() {
    return external;
  }

  public void setExternal(boolean external) {
    this.external = external;
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

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Score)) {
      return false;
    }

    Score score = (Score) obj;
    return score.getPlayerInitials().equalsIgnoreCase(this.getPlayerInitials())
        && score.getPosition() == this.getPosition()
        && score.getScore() == this.getScore();
  }

  public boolean matches(Score newScore) {
    return this.playerInitials != null && this.playerInitials.equals(newScore.getPlayerInitials())
        && this.score == newScore.getScore();

  }

  @Override
  public String toString() {
    return toString(Locale.getDefault());
  }

  public String toString(Locale loc) {
    String name = this.getPlayerInitials();
    if (this.player != null) {
      name = this.player.getName();
    }
    return "#" + this.getPosition() + " " + name + "   " + getFormattedScore(loc);
  }

  @JsonIgnore
  public String getFormattedScore() {
    return getFormattedScore(Locale.getDefault());
  }

  @JsonIgnore
  public String getFormattedScore(Locale loc) {
    String formattedScore = ScoreFormatUtil.formatScore(this.getScore(), loc);
    //TODO was it really needed  
    // Maybe because if NumberFormatException is caught in formatScore, it returns "0" but that should never happen 
    //if (!formattedScore.equals("0")) {
      return formattedScore;
    //}
    //return rawScore;
  }

  public Score cloneEmpty() {
    return new Score(this.createdAt, this.gameId, "???", this.player, this.rawScore, this.score, this.position);
  }

  @JsonIgnore
  public boolean isDenied(DeniedScore deniedScore) {
    try {
//      return String.valueOf(deniedScore.getScore()).equals(String.valueOf(numScore))
//          && deniedScore.getInitials().equalsIgnoreCase(String.valueOf(playerInitials));
      return deniedScore.getScore() == this.score;
    }
    catch (Exception e) {
      return false;
    }
  }

  @JsonIgnore
  public boolean isSkipped() {
    return getPlayerInitials().equals("???") || getScore() == 0;
  }
}
