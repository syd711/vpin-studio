package de.mephisto.vpin.connectors.wovp.models;

public class ApiKeyValidationResponse {
  private boolean success;
  private String userId;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
