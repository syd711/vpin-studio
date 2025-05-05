package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class HeaderController implements Initializable {

  private double xOffset;
  private double yOffset;

  @FXML
  private BorderPane header;

  @FXML
  private Label titleLabel;

  @FXML
  private void onCloseClick() {
    System.exit(0);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }
}
