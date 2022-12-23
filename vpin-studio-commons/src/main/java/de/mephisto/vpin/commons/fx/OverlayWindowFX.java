package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.restclient.OverlayClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class OverlayWindowFX extends Application {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(OverlayWindowFX.class);

  public static final CountDownLatch latch = new CountDownLatch(1);
  public static OverlayWindowFX overlayFX = null;

  private Stage stage;

  private BorderPane root;

  public static OverlayClient client;
  private OverlayController overlayController;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public static OverlayWindowFX waitForOverlay() {
    try {
      latch.await();
      LOG.info("OverlayFX creation finished.");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return overlayFX;
  }

  public void setVisible(boolean b) {
    if (b) {
      stage.show();
      overlayController.refreshData();
    }
    else {
      stage.hide();
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.stage = primaryStage;
    Platform.setImplicitExit(false);

    root = new BorderPane();
    Screen screen = Screen.getPrimary();
    final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);

    Rectangle2D bounds = screen.getVisualBounds();
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());

    stage.setScene(scene);
    stage.setFullScreenExitHint("");
    stage.setAlwaysOnTop(true);
    stage.setFullScreen(true);
//    stage.initStyle(StageStyle.UNDECORATED);
//    scene.setFill(Color.web("#272b2f"));
    stage.getScene().getStylesheets().add(OverlayWindowFX.class.getResource("stylesheet.css").toExternalForm());

    overlayFX = this;

    try {
      String resource = "scene-overlay-uhd.fxml";
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      if(screenBounds.getWidth() < 3000) {
        resource = "scene-overlay-wqhd.fxml";
        LOG.info("Using WQHD Dashboard");
      }
      else {
        LOG.info("Using UHD Dashboard");
      }

      FXMLLoader loader = new FXMLLoader(OverlayController.class.getResource(resource));
      Parent widgetRoot = loader.load();
      overlayController = loader.getController();
      root.setCenter(widgetRoot);
    } catch (IOException e) {
      LOG.error("Failed to init dashboard: " + e.getMessage(), e);
    }

    latch.countDown();
  }
}
