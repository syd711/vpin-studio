package de.mephisto.vpin.server.util;

public interface UnzipChangeListener {

  boolean unzipping(String name, int index, int total);

  void onError(String error);
}
