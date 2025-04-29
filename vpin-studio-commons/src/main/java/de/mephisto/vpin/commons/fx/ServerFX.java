package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.FrontendScreensManager;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.mania.ManiaServiceClient;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import java.util.ArrayList;
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
public class ServerFX extends Application {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(ServerFX.class);

  public static final CountDownLatch latch = new CountDownLatch(1);
  public static int TO_FRONT_DELAY = 2500;

  private BorderPane root;

  public static OverlayClient client;
  public static VPinManiaClient maniaClient;
  private OverlayController overlayController;

  private static ServerFX INSTANCE = null;

  private Stage overlayStage;
  private Stage maintenanceStage;

  private boolean overlayVisible = false;

  private FrontendScreenController highscoreCardController;

  public static ServerFX getInstance() {
    return INSTANCE;
  }

  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "false");
    Application.launch(args);
  }

  public static List<ServerFXListener> listeners = new ArrayList<>();

  public static void addListener(ServerFXListener listener) {
    listeners.add(listener);
  }

  public boolean isOverlayVisible() {
    return overlayVisible;
  }

  public static void waitForOverlay() {
    try {
      latch.await();
      LOG.info("OverlayFX creation finished.");
      for (ServerFXListener listener : listeners) {
        listener.fxInitialized();
      }
    }
    catch (InterruptedException e) {
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
        OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
        String value = overlaySettings.getDesignType();
        if (StringUtils.isEmpty(value) || value.equalsIgnoreCase("null")) {
          value = "";
        }
        String resource = resolveDashboard(value);

        FXMLLoader loader = new FXMLLoader(OverlayController.class.getResource(resource));
        Parent widgetRoot = loader.load();
        overlayController = loader.getController();
        root.setCenter(widgetRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to init dashboard: " + e.getMessage(), e);
      }
      new Thread(() -> {
        ServerFX.toFront(overlayStage, overlayVisible);
        ServerFX.toFront(overlayStage, overlayVisible);
        ServerFX.toFront(overlayStage, overlayVisible);
        ServerFX.toFront(overlayStage, overlayVisible);
      }).start();
      ServerFX.forceShow(overlayStage);

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
    }
    catch (InterruptedException e) {
      //ignore
    }
    Platform.runLater(() -> {
      if (visible) {
        stages.stream().forEach(Stage::toFront);
        LOG.info("Forcing to front for stage.");
      }
    });
  }

  public static String resolveDashboard(String value) {
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

    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    Rectangle2D screenBounds = SystemUtil.getScreenById(overlaySettings.getOverlayScreenId()).getBounds();
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
    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    Screen screen = SystemUtil.getScreenById(overlaySettings.getOverlayScreenId());
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
    maintenanceStage.getScene().getStylesheets().add(ServerFX.class.getResource("stylesheet.css").toExternalForm());

    try {
      String resource = "scene-maintenance.fxml";
      FXMLLoader loader = new FXMLLoader(MaintenanceController.class.getResource(resource));
      Parent widgetRoot = loader.load();
      MaintenanceController controller = loader.getController();
      root.setCenter(widgetRoot);
    }
    catch (IOException e) {
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
      PauseMenu.togglePauseMenu(gameStatus, false);
    });

    try {
      Thread.sleep(duration * 1000);

    }
    catch (InterruptedException e) {
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

  public void showHighscoreCard(@NonNull CardSettings cardSettings, @Nullable FrontendPlayerDisplay display, String mimeType, File file) {
    try {
      int notificationTime = cardSettings.getNotificationTime();
      LOG.info("Showing highscore card " + file.getAbsolutePath() + " for " + notificationTime + "ms");
      if (notificationTime > 0) {
        int rotation = 0;
        String rotationValue = cardSettings.getNotificationRotation();
        if (rotationValue != null) {
          try {
            rotation = Integer.parseInt(rotationValue);
          }
          catch (NumberFormatException e) {
            LOG.info("Error reading card rotation value: " + e.getMessage());
          }
        }

        FrontendScreenAsset asset = new FrontendScreenAsset();
        asset.setName(file.getName());
        asset.setDisplay(display);
        asset.setRotation(rotation);
        asset.setDuration(notificationTime);
        asset.setMimeType(mimeType);
        asset.setInputStream(new FileInputStream(file));

        FrontendScreensManager.getInstance().showScreen(asset);
      }
      else {
        LOG.info("Skipping highscore card overlay, zero time set.");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open highscore card notification: " + e.getMessage());
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    INSTANCE = this;

    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);

    this.overlayStage = primaryStage;
    Platform.setImplicitExit(false);

    root = new BorderPane();
    Screen screen = SystemUtil.getScreenById(overlaySettings.getOverlayScreenId());
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
    overlayStage.getScene().getStylesheets().add(ServerFX.class.getResource("stylesheet.css").toExternalForm());

    PauseMenu.loadPauseMenu();
    latch.countDown();
  }
}
