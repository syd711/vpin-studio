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

  public void setLoadingMessage(String msg) {
    this.messageLabel.setText(msg);
  }
}
