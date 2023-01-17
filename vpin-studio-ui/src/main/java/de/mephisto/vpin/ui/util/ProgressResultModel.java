package de.mephisto.vpin.ui.util;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ProgressResultModel {
  private int processed;
  private int skipped;

  public List<Object> results = new ArrayList<>();
  private ProgressBar progressBar;

  public ProgressResultModel(ProgressBar progressBar) {
    this.progressBar = progressBar;
  }

  public void setProgress(double progress) {
    this.progressBar.setProgress(progress);
  }

  public void setIndeterminate() {
    this.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
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
