package de.mephisto.vpin.ui.components.screens;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ScreensController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreensController.class);

  @FXML
  private BorderPane screenRoot;

  @FXML
  private Button zoomInBtn;

  @FXML
  private Button zoomOutBtn;

  private ManagedScreenController controller;

  private double zoom = 1;

  @FXML
  private void onReload() {
    client.getFrontendService().getScreenSummary(true);
    controller.reload();
  }

  @FXML
  private void zoomOut() {
    if (zoom > 0.6) {
      zoom = zoom - 0.1;
      controller.setZoom(zoom);
    }
  }

  @FXML
  private void zoomIn() {
    if (zoom < 1) {
      zoom = zoom + 0.1;
      controller.setZoom(zoom);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(ManagedScreenController.class.getResource("managed-screen.fxml"));
      Parent builtInRoot = loader.load();
      controller = loader.getController();
      screenRoot.setCenter(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load managed screen: " + e.getMessage(), e);
    }

    Platform.runLater(() -> {
      controller.reload();
    });
  }
}
