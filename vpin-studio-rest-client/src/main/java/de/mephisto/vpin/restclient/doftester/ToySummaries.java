package de.mephisto.vpin.restclient.doftester;

import java.util.HashMap;
import java.util.Map;

public class ToySummaries {
  private Map<Integer, ToySummary> summaries = new HashMap<>();

  public Map<Integer, ToySummary> getSummaries() {
    return summaries;
  }

  public void setSummaries(Map<Integer, ToySummary> summaries) {
    this.summaries = summaries;
  }

  public ToySummary get(int gameId) {
    return summaries.get(gameId);
  }
}
