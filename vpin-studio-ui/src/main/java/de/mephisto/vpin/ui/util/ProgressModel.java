package de.mephisto.vpin.ui.util;

abstract public class ProgressModel<T> {

  private String title;

  public ProgressModel(String title) {
    this.title = title;
  }

  public void setTitle(String title) {
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

  public boolean isCancelable() {
    return true;
  }

  public boolean isShowSteps() {
    return true;
  }

  abstract public int getMax();

  abstract public T getNext();

  abstract public String nextToString(T t);

  abstract public void processNext(ProgressResultModel progressResultModel, T next) throws Exception;

  abstract public boolean hasNext();

  public void cancel() {

  }

  public void finalizeModel(ProgressResultModel progressResultModel) {

  }
}
