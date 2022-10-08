package de.mephisto.vpin.ui.util;

public class ProgressResultModel {
  private int processed;
  private int skipped;

  public ProgressResultModel() {
  }

  public void addProcessed() {
    this.processed++;
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
