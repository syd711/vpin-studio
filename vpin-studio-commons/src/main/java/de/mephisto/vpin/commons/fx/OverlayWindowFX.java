package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.PopperScreensManager;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.model.PopperScreenAsset;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Coordinates Fixing:
 * <p>
 * x
 * |
 * -y ------- +y
 * |
 * -x
 */
public class OverlayWindowFX extends Application {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(OverlayWindowFX.class);

  public static final CountDownLatch latch = new CountDownLatch(1);
  public static int TO_FRONT_DELAY = 2500;

  private BorderPane root;

  public static OverlayClient client;
  private OverlayController overlayController;

  private static OverlayWindowFX INSTANCE = null;

  private Stage overlayStage;
  private Stage maintenanceStage;

  private boolean overlayVisible = false;

  private PopperScreenController highscoreCardController;

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

  public void showOverlay(boolean visible) {
    if (overlayVisible && visible) {
      return;
    }

    overlayVisible = visible;
    if (visible) {
      try {
        PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.OVERLAY_DESIGN);
        String value = preference.getValue();
        if (StringUtils.isEmpty(value) || value.equalsIgnoreCase("null")) {
          value = "";
        }
        String resource = resolveDashboard(value);

        FXMLLoader loader = new FXMLLoader(OverlayController.class.getResource(resource));
        Parent widgetRoot = loader.load();
        overlayController = loader.getController();
        root.setCenter(widgetRoot);
      } catch (IOException e) {
        LOG.error("Failed to init dashboard: " + e.getMessage(), e);
      }
      new Thread(() -> {
        OverlayWindowFX.toFront(overlayStage, overlayVisible);
        OverlayWindowFX.toFront(overlayStage, overlayVisible);
        OverlayWindowFX.toFront(overlayStage, overlayVisible);
        OverlayWindowFX.toFront(overlayStage, overlayVisible);
      }).start();
      OverlayWindowFX.forceShow(overlayStage);

      overlayController.refreshData();
    }
    else {
      overlayStage.hide();
    }
  }


  public static void forceShow(Stage stage) {
    Platform.runLater(() -> {
      stage.toFront();
    });
    stage.show();
  }

  public static void toFront(Stage stage, boolean visible) {
    toFront(Arrays.asList(stage), visible);
  }

  public static void toFront(List<Stage> stages, boolean visible) {
    try {
      Thread.sleep(TO_FRONT_DELAY);
      stages.forEach(s -> s.getScene().setCursor(Cursor.NONE));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    Platform.runLater(() -> {
      if (visible) {
        stages.stream().forEach(Stage::toFront);
      }
    });
  }

  private static String resolveDashboard(String value) {
    String fxml = "scene-overlay-" + resolveDashboardResolution();
    if (!StringUtils.isEmpty(value)) {
      fxml = fxml + value;
    }

    fxml = fxml + ".fxml";
    LOG.info("Using Dashboard " + fxml);
    return fxml;
  }

  private static String resolveDashboardResolution() {
    String resource = "uhd";
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    double width = screenBounds.getWidth();
    if (screenBounds.getWidth() < screenBounds.getHeight()) {
      width = screenBounds.getHeight();
    }

    if (width < 3000 && width > 2000) {
      resource = "wqhd";
    }
    else if (width < 2000) {
      resource = "hd";
    }
    return resource;
  }

  public void setMaintenanceVisible(boolean b) {
    if (maintenanceStage != null) {
      if (b) {
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
    scene.setCursor(Cursor.NONE);

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

    if (b) {
      maintenanceStage.setFullScreen(true);
      maintenanceStage.show();
    }
  }

  public void togglePauseMenu() {
    Platform.runLater(() -> {
      PauseMenu.togglePauseMenu();
    });
  }

  public void testPauseMenu(int gameId, int duration) {
    Platform.runLater(() -> {
      LOG.info("Received pause menu test event for game id " + gameId);
      GameStatus gameStatus = new GameStatus();
      gameStatus.setGameId(gameId);
      gameStatus.setStarted(new Date());
      PauseMenu.togglePauseMenu(gameStatus);
    });

    try {
      Thread.sleep(duration * 1000);

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    Platform.runLater(() -> {
      PauseMenu.togglePauseMenu();
    });
  }

  public void exitPauseMenu() {
    Platform.runLater(() -> {
      PauseMenu.exitPauseMenu();
    });
  }

  public void showHighscoreCard(CardSettings cardSettings, PinUPPlayerDisplay display, String mimeType, File file) {
    try {
      int notificationTime = cardSettings.getNotificationTime();
      if (notificationTime > 0) {
        int rotation = 0;
        String rotationValue = cardSettings.getNotificationRotation();
        if (rotationValue != null) {
          try {
            rotation = Integer.parseInt(rotationValue);
          } catch (NumberFormatException e) {
            LOG.info("Error reading card rotation value: " + e.getMessage());
          }
        }

        PopperScreenAsset asset = new PopperScreenAsset();
        asset.setName(file.getName());
        asset.setDisplay(display);
        asset.setRotation(rotation);
        asset.setDuration(notificationTime);
        asset.setMimeType(mimeType);
        asset.setInputStream(new FileInputStream(file));

        PopperScreensManager.getInstance().showScreen(asset);
      }
      else {
        LOG.info("Skipping highscore card overlay, zero time set.");
      }
    } catch (Exception e) {
      LOG.error("Failed to open highscore card notification: " + e.getMessage());
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    INSTANCE = this;

    this.overlayStage = primaryStage;
    Platform.setImplicitExit(false);

    root = new BorderPane();
    Screen screen = Screen.getPrimary();
    final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);
    scene.setCursor(Cursor.NONE);

    Rectangle2D bounds = screen.getVisualBounds();
    overlayStage.setX(bounds.getMinX());
    overlayStage.setY(bounds.getMinY());

    overlayStage.setScene(scene);
    overlayStage.setFullScreenExitHint("");
    overlayStage.setAlwaysOnTop(true);
    overlayStage.setFullScreen(true);
    overlayStage.setTitle("VPin Studio Overlay");
    overlayStage.getScene().getStylesheets().add(OverlayWindowFX.class.getResource("stylesheet.css").toExternalForm());

    PauseMenu.loadPauseMenu();
    latch.countDown();
  }
}
