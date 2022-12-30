package de.mephisto.vpin.ui.util;

abstract public class ProgressModel {

  private String title;

  public ProgressModel(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public boolean isShowSummary() {
    return true;
  }

  public boolean isIndeterminate() {
    return false;
  }

  abstract public int getMax();

  abstract public String processNext(ProgressResultModel progressResultModel);

  abstract public boolean hasNext();
}
