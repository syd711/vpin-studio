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

/**
 * Coordinates Fixing:
 *
 *        x
 *        |
 *  -y ------- +y
 *        |
 *       -x
 */
public class OverlayWindowFX extends Application {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(OverlayWindowFX.class);

  public static final CountDownLatch latch = new CountDownLatch(1);
  private Stage stage;

  private BorderPane root;

  public static OverlayClient client;
  private OverlayController overlayController;

  private static OverlayWindowFX INSTANCE = null;
  private Stage maintenanceStage;

  public static OverlayWindowFX getInstance() {
    return INSTANCE;
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  public static void waitForOverlay() {
    try {
      latch.await();
      LOG.info("OverlayFX creation finished.");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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

  public void setMaintenanceVisible(boolean b) {
    if(maintenanceStage != null) {
      if(b) {
        maintenanceStage.setFullScreen(true);
        maintenanceStage.show();
      }
      else {
        maintenanceStage.hide();
      }
      return;
    }

    BorderPane root = new BorderPane();
    Screen screen = Screen.getPrimary();
    final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);

    maintenanceStage = new Stage();
    Rectangle2D bounds = screen.getVisualBounds();
    maintenanceStage.setX(bounds.getMinX());
    maintenanceStage.setY(bounds.getMinY());

    maintenanceStage.setScene(scene);
    maintenanceStage.setFullScreenExitHint("");
    maintenanceStage.setAlwaysOnTop(true);
    maintenanceStage.setFullScreen(true);
    maintenanceStage.getScene().getStylesheets().add(OverlayWindowFX.class.getResource("stylesheet.css").toExternalForm());

    try {
      String resource = "scene-maintenance.fxml";
      FXMLLoader loader = new FXMLLoader(MaintenanceController.class.getResource(resource));
      Parent widgetRoot = loader.load();
      MaintenanceController controller = loader.getController();
      root.setCenter(widgetRoot);
    } catch (IOException e) {
      LOG.error("Failed to init dashboard: " + e.getMessage(), e);
    }

    if(b) {
      maintenanceStage.setFullScreen(true);
      maintenanceStage.show();
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    INSTANCE = this;

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

    try {
      String resource = "scene-overlay-uhd.fxml";
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      if(screenBounds.getWidth() < 3000 && screenBounds.getWidth() > 2000) {
        resource = "scene-overlay-wqhd.fxml";
        LOG.info("Using WQHD Dashboard");
      }
      else if(screenBounds.getWidth() < 2000) {
        resource = "scene-overlay-hd.fxml";
        LOG.info("Using HD Dashboard");
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
