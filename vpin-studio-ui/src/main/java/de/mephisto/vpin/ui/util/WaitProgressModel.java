package de.mephisto.vpin.ui.util;

import java.util.concurrent.Callable;

public class WaitProgressModel<T> extends ProgressModel<Void> {

  private String message;
  private Callable<T> callable;
  private boolean done = false;

  public WaitProgressModel(String title, String message, Callable<T> callable) {
    super(title);
    this.message = message;
    this.callable = callable;
  }

  public boolean isShowSummary() {
    return false;
  }
  public boolean isIndeterminate() {
    return true;
  }

  public int getMax() {
    return 1;
  }

  public Void getNext() {
    return null;
  }

  public String nextToString(Void t) {
    return message;
  }

  public void processNext(ProgressResultModel progressResultModel, Void next) throws Exception {
    T result = callable.call();
    done = true;
    progressResultModel.addProcessed();
    progressResultModel.getResults().add(result);
  }

  public boolean hasNext() {
    return !done;
  }

}
