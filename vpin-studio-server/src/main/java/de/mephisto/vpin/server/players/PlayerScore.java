package de.mephisto.vpin.server.players;

import de.mephisto.vpin.server.highscores.Score;

import java.util.Date;

public class PlayerScore {
  private Score score;
  private String tableName;
  private String wheelUrl;
  private Date updatedAt;

  public PlayerScore(Score score, Date updatedAt, String tableName, String wheelUrl) {
    this.score = score;
    this.updatedAt = updatedAt;
    this.tableName = tableName;
    this.wheelUrl = wheelUrl;
  }

  public String getWheelUrl() {
    return wheelUrl;
  }

  public void setWheelUrl(String wheelUrl) {
    this.wheelUrl = wheelUrl;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Score getScore() {
    return score;
  }

  public void setScore(Score score) {
    this.score = score;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
}
