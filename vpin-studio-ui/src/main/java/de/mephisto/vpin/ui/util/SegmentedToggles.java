package de.mephisto.vpin.ui.util;

import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * ToggleButtons of one ToggleGroup used as a segmented control (see UI-STYLEGUIDE.md): a click on the selected
 * button must not deselect it, so that the group always keeps one selection, like radio buttons.
 */
public final class SegmentedToggles {

  private SegmentedToggles() {
  }

  public static void keepSelected(ToggleButton... toggles) {
    for (ToggleButton toggle : toggles) {
      toggle.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
        if (toggle.isSelected()) {
          e.consume();
        }
      });
      toggle.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
        if (toggle.isSelected() && e.getCode() == KeyCode.SPACE) {
          e.consume();
        }
      });
    }
  }
}
