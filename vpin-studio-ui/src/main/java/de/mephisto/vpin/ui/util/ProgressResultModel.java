package de.mephisto.vpin.ui.util;

import java.util.ArrayList;
import java.util.List;

public class ProgressResultModel {
  private int processed;
  private int skipped;

  public List<Object> results = new ArrayList<>();

  public ProgressResultModel() {
  }

  public void addProcessed() {
    this.processed++;
  }

  public void addProcessed(Object result) {
    this.processed++;
    this.results.add(result);
  }

  public List<Object> getResults() {
    return results;
  }

  public void addSkipped() {
    this.skipped++;
  }

  public int getProcessed() {
    return processed;
  }

  public int getSkipped() {
    return skipped;
  }
}
