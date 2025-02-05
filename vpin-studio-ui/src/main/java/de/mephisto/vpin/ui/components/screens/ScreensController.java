package de.mephisto.vpin.ui.components.screens;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ScreensController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreensController.class);

  @FXML
  private Pane screenRoot;

  private ManagedScreenController controller;

  @FXML
  private void onReload() {
    controller.reload();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(ManagedScreenController.class.getResource("managed-screen.fxml"));
      Parent builtInRoot = loader.load();
      controller = loader.getController();
      screenRoot.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load managed screen: " + e.getMessage(), e);
    }

    Platform.runLater(() -> {
      controller.reload();
    });
  }
}
