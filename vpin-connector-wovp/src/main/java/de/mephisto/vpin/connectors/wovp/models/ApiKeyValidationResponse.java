package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

public class ApiKeyValidationResponse {
  private boolean success;
  private String userId;
  private String firstName;
  private String lastName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

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

  @JsonIgnore
  public String getName() {
    String name = "";
    if (!StringUtils.isEmpty(firstName)) {
      name += (firstName + " ");
    }
    if (!StringUtils.isEmpty(lastName)) {
      name += lastName;
    }

    return name;
  }
}
