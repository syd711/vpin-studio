package de.mephisto.vpin.restclient.mania;

import java.util.ArrayList;
import java.util.List;

public class ManiaRegistration {
  private List<Long> playerIds = new ArrayList<>();
  private boolean synchronizeRatings = false;
  private String result;

  public boolean isSynchronizeRatings() {
    return synchronizeRatings;
  }

  public void setSynchronizeRatings(boolean synchronizeRatings) {
    this.synchronizeRatings = synchronizeRatings;
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
