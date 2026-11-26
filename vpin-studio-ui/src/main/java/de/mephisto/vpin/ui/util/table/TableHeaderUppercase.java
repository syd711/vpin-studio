package de.mephisto.vpin.ui.util.table;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.Locale;

/**
 * JavaFX CSS has no text-transform, so the uppercase column headers of the table style
 * (see stylesheet-tableview.css) are applied here. The header label is not bound to the column text
 * (TableColumnHeader calls setText), so a listener can rewrite it whenever the column text changes.
 */
final class TableHeaderUppercase {
  private static final String KEY = TableHeaderUppercase.class.getName();

  private TableHeaderUppercase() {
  }

  static void apply(Node headerRow) {
    if (headerRow == null) {
      return;
    }
    for (Node node : headerRow.lookupAll(".column-header .label")) {
      if (node instanceof Label label && label.getProperties().putIfAbsent(KEY, Boolean.TRUE) == null) {
        label.textProperty().addListener((obs, oldValue, newValue) -> uppercase(label));
        uppercase(label);
      }
    }
  }

  private static void uppercase(Label label) {
    String text = label.getText();
    if (text != null) {
      String upper = text.toUpperCase(Locale.getDefault());
      if (!upper.equals(text)) {
        label.setText(upper);
      }
    }
  }
}
