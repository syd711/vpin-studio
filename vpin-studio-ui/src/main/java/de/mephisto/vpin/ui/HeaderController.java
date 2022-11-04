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
      Studio.stage.setX(primaryScreenBounds.getMinX());
      Studio.stage.setY(primaryScreenBounds.getMinY());
      Studio.stage.setMaxHeight(primaryScreenBounds.getHeight());
      Studio.stage.setMinHeight(primaryScreenBounds.getHeight());
      Studio.stage.setMaxWidth(primaryScreenBounds.getWidth());
      Studio.stage.setMinWidth(primaryScreenBounds.getWidth());
    }
    else {
      Studio.stage.setX(400);
      Studio.stage.setY(200);
      Studio.stage.setMaxHeight(1280);
      Studio.stage.setMinHeight(1280);
      Studio.stage.setMaxWidth(1920);
      Studio.stage.setMinWidth(1920);
    }
    toggleMaximize = !toggleMaximize;
  }

  @FXML
  private void onHideClick() {
    Studio.stage.setIconified(true);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
