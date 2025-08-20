package de.mephisto.vpin.restclient.util;

public interface UnzipChangeListener {

  boolean unzipping(String name, int index, int total);

  void onError(String error);
}
