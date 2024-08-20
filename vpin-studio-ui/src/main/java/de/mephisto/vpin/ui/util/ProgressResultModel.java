package de.mephisto.vpin.ui.util;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgressResultModel {
  private int processed;
  private int skipped;
  private boolean cancelled = false;

  public List<Object> results = new ArrayList<>();
  private ProgressBar progressBar;
  private final Label statusLabel;

  public ProgressResultModel(ProgressBar progressBar, Label statusLabel) {
    this.progressBar = progressBar;
    this.statusLabel = statusLabel;
  }

  public void setProgress(double progress) {
    if (this.progressBar.isVisible()) {
      this.progressBar.setProgress(progress);
    }
  }

  public void setStatusMessage(String msg) {
    this.statusLabel.setText(msg);
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

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  public List<Object> getResults() {
    return results;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getTypedResults() {
    return results.stream().map(o -> (T) o).collect(Collectors.toList());
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
