package de.mephisto.vpin.ui.achievements.tables;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Tables tile: hint shown instead of the achievements when the table has no map or no rules.
 */
public class TableInfoTileController {

  @FXML
  private Label titleLabel;
  @FXML
  private Label textLabel;

  public void render(String title, String text) {
    titleLabel.setText(title);
    textLabel.setText(text);
  }
}
