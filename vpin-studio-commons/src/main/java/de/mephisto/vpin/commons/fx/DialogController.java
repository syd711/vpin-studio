package de.mephisto.vpin.commons.fx;

public interface DialogController {

  void onDialogCancel();

  default void onResized(int x, int y, int width, int height) {

  }
}
