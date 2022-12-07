package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ToolbarController implements Initializable {

  // Add a public no-args constructor
  public ToolbarController() {
  }

  @FXML
  private void onDisconnect() {
  }

  @FXML
  private void onClearCache() {
    client.clearCache();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}