package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.net.URL;
import java.util.ResourceBundle;

public class HeaderController implements Initializable {

  private static boolean toggleMaximize = true;

  @FXML
  private void onCloseClick() {
    System.exit(0);
  }

  @FXML
  private void onMaximize() {
    if(toggleMaximize) {
      Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
      VPinStudioApplication.stage.setX(primaryScreenBounds.getMinX());
      VPinStudioApplication.stage.setY(primaryScreenBounds.getMinY());
      VPinStudioApplication.stage.setMaxHeight(primaryScreenBounds.getHeight());
      VPinStudioApplication.stage.setMinHeight(primaryScreenBounds.getHeight());
      VPinStudioApplication.stage.setMaxWidth(primaryScreenBounds.getWidth());
      VPinStudioApplication.stage.setMinWidth(primaryScreenBounds.getWidth());
    }
    else {
      VPinStudioApplication.stage.setX(400);
      VPinStudioApplication.stage.setY(200);
      VPinStudioApplication.stage.setMaxHeight(1280);
      VPinStudioApplication.stage.setMinHeight(1280);
      VPinStudioApplication.stage.setMaxWidth(1920);
      VPinStudioApplication.stage.setMinWidth(1920);
    }
    toggleMaximize = !toggleMaximize;
  }

  @FXML
  private void onHideClick() {
    VPinStudioApplication.stage.setIconified(true);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
