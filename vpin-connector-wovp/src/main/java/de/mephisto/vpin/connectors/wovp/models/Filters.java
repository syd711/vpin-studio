package de.mephisto.vpin.connectors.wovp.models;

import java.util.Arrays;
import java.util.List;

public class Filters {
  private List<Integer> statuses = Arrays.asList(1);
  private boolean inProgress = true;

  public List<Integer> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<Integer> statuses) {
    this.statuses = statuses;
  }

  public boolean isInProgress() {
    return inProgress;
  }

  public void setInProgress(boolean inProgress) {
    this.inProgress = inProgress;
  }
}
