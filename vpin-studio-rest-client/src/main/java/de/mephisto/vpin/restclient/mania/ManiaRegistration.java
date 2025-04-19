package de.mephisto.vpin.restclient.mania;

import java.util.ArrayList;
import java.util.List;

public class ManiaRegistration {
  private List<Long> playerIds = new ArrayList<>();
  private boolean submitRatings = false;
  private boolean submitPlayCount = false;
  private boolean submitTables = false;
  private String result;

  public boolean isSubmitPlayCount() {
    return submitPlayCount;
  }

  public boolean isSubmitTables() {
    return submitTables;
  }

  public void setSubmitTables(boolean submitTables) {
    this.submitTables = submitTables;
  }

  public void setSubmitPlayCount(boolean submitPlayCount) {
    this.submitPlayCount = submitPlayCount;
  }

  public boolean isSubmitRatings() {
    return submitRatings;
  }

  public void setSubmitRatings(boolean submitRatings) {
    this.submitRatings = submitRatings;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public List<Long> getPlayerIds() {
    return playerIds;
  }

  public void setPlayerIds(List<Long> playerIds) {
    this.playerIds = playerIds;
  }
}
