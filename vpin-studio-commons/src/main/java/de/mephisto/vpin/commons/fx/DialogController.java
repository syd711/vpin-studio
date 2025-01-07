package de.mephisto.vpin.commons.fx;

import javafx.scene.input.KeyEvent;

public interface DialogController {

  void onDialogCancel();

  default void onKeyPressed(KeyEvent ke) {

  }

  default void onResized(int x, int y, int width, int height) {

  }

  default void setModality(boolean modal) {

  }
}
