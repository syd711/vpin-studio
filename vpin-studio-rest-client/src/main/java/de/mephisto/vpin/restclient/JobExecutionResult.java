package de.mephisto.vpin.restclient;

import org.apache.commons.lang3.StringUtils;

public class JobExecutionResult {
  private int gameId;
  private String resourceId;
  private String error;

  public String getResourceId() {
    return resourceId;
  }

  public boolean isErrorneous() {
    return !StringUtils.isEmpty(error);
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
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
