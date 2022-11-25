package de.mephisto.vpin.restclient.representations;

import java.util.Date;

public class PlayerScoreRepresentation {
  private ScoreRepresentation score;
  private String tableName;
  private Date updatedAt;
  private String wheelUrl;

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

  public ScoreRepresentation getScore() {
    return score;
  }

  public void setScore(ScoreRepresentation score) {
    this.score = score;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
}
