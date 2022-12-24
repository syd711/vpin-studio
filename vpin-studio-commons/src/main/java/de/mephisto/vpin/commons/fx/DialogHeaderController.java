package de.mephisto.vpin.commons.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogHeaderController implements Initializable {

  private double xOffset;
  private double yOffset;

  @FXML
  private BorderPane header;

  @FXML
  private Label titleLabel;

  private Stage stage;

  @FXML
  private void onCloseClick() {
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setUserData(this);
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }
}
