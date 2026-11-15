package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class WaitOverlayController implements Initializable {

  @FXML
  private Label messageLabel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @FXML
  private Label hintLabel;

  public void setLoadingMessage(String msg) {
    this.messageLabel.setText(msg);
  }

  /**
   * Optional smaller, wrapped text shown below the loading message.
   */
  public void setHint(String hint) {
    boolean show = hint != null && !hint.isBlank();
    this.hintLabel.setText(show ? hint : "");
    this.hintLabel.setVisible(show);
    this.hintLabel.setManaged(show);
  }
}
