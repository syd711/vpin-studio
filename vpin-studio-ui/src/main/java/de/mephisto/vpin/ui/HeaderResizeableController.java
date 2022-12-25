package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class HeaderResizeableController implements Initializable {

  private static boolean toggleMaximize = true;

  private double xOffset;
  private double yOffset;

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private BorderPane header;

  @FXML
  private void onCloseClick() {
    System.exit(0);
  }

  @FXML
  private void onMaximize() {
    if(toggleMaximize) {
      Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
      stage.setX(primaryScreenBounds.getMinX());
      stage.setY(primaryScreenBounds.getMinY());
      stage.setMaxHeight(primaryScreenBounds.getHeight());
      stage.setMinHeight(primaryScreenBounds.getHeight());
      stage.setMaxWidth(primaryScreenBounds.getWidth());
      stage.setMinWidth(primaryScreenBounds.getWidth());
    }
    else {
      stage.setX(400);
      stage.setY(200);
      stage.setMaxHeight(1280);
      stage.setMinHeight(1280);
      stage.setMaxWidth(1920);
      stage.setMinWidth(1920);
    }
    toggleMaximize = !toggleMaximize;
  }

  @FXML
  private void onHideClick() {
    stage.setIconified(true);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    titleLabel.setText("VPin Studio");
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });
  }
}
