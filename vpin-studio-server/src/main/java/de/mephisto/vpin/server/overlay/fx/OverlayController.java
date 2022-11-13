package de.mephisto.vpin.server.overlay.fx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

public class OverlayController implements Initializable {

  @FXML
  private BorderPane root;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    root.setStyle("-fx-background-color: #CCCCCC;");
  }
}
