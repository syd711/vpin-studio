package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RootController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(RootController.class);

  @FXML
  private StackPane rootStack;

  private static Parent mediaOverlay;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-media.fxml"));
      mediaOverlay = loader.load();
      MediaOverlayController ctrl = loader.getController();
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public static void showMedia() {

  }

}
