package de.mephisto.vpin.restclient.jobs;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class JobExecutionResult {
  private final String uuid;
  private int gameId;
  private String error;
  private String message;
  private String imgUrl;
  private String externalUrl;

  public JobExecutionResult() {
    this.uuid = UUID.randomUUID().toString();
  }

  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getUuid() {
    return uuid;
  }

  public boolean isErrorneous() {
    return !StringUtils.isEmpty(error);
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
