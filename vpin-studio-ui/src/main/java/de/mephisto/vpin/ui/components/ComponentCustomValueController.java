package de.mephisto.vpin.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ComponentCustomValueController {

  @FXML
  private Label titleLabel;

  @FXML
  private Label valueLabel;

  public void refresh(String key, String value) {
    titleLabel.setText(key);
    valueLabel.setText(value);
  }
}
