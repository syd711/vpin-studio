package de.mephisto.vpin.ui.util;

import java.util.Iterator;

abstract public class ProgressModel {

  private String title;

  public ProgressModel(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  abstract public int getMax();

  abstract public Iterator getIterator();

  abstract public String processNext(ProgressResultModel progressResultModel);
}
