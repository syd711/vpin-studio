package de.mephisto.vpin.ui;

import org.jspecify.annotations.Nullable;
import javafx.scene.input.KeyEvent;


public interface StudioFXController {

  void onViewActivated(@Nullable NavigationOptions options);

  default void onViewDeactivated() {

  }

  default void onKeyEvent(KeyEvent event) {

  }
}
