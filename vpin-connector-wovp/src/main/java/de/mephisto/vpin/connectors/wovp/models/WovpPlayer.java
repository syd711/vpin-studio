package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class WovpPlayer {
  private String id;
  private String name;
  private String apiKey;

  @JsonIgnore
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    WovpPlayer that = (WovpPlayer) o;
    return Objects.equals(id, that.id) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString() {
    return name;
  }
}
