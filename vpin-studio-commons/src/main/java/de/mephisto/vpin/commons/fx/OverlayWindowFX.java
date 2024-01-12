package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
  private Stage stage;

  private BorderPane root;

  public static OverlayClient client;
  private OverlayController overlayController;

  private static OverlayWindowFX INSTANCE = null;
  private Stage maintenanceStage;
  private Stage highscoreCardStage;
  private HighscoreCardController highscoreCardController;

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

  public void showOverlay(boolean b) {
    if (b) {
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
      stage.show();
      overlayController.refreshData();
    } else {
      stage.hide();
    }
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
    if (screenBounds.getWidth() < 3000 && screenBounds.getWidth() > 2000) {
      resource = "wqhd";
    } else if (screenBounds.getWidth() < 2000) {
      resource = "hd";
    }
    return resource;
  }

  public void setMaintenanceVisible(boolean b) {
    if (maintenanceStage != null) {
      if (b) {
        maintenanceStage.setFullScreen(true);
        maintenanceStage.show();
      } else {
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

  public void showPauseMenu() {
    PauseMenu.showPauseMenu();
  }

  public void showHighscoreCard(CardSettings cardSettings, PinUPPlayerDisplay display, File file) {
    try {
      int notificationTime = cardSettings.getNotificationTime();
      if (notificationTime > 0) {
        LOG.info("Showing highscore card " + file.getAbsolutePath());
        if (highscoreCardStage != null) {
          highscoreCardController.setImage(highscoreCardStage, cardSettings, display, file);
          showHighscoreCard(notificationTime);
          return;
        }

        BorderPane root = new BorderPane();
        Screen screen = Screen.getPrimary();
        final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.TRANSPARENT);
        scene.setCursor(Cursor.NONE);

        highscoreCardStage = new Stage();
        highscoreCardStage.setScene(scene);
        highscoreCardStage.initStyle(StageStyle.TRANSPARENT);
        highscoreCardStage.setAlwaysOnTop(true);

        try {
          String resource = "scene-highscore-card.fxml";
          FXMLLoader loader = new FXMLLoader(HighscoreCardController.class.getResource(resource));
          Parent widgetRoot = loader.load();
          highscoreCardController = loader.getController();
          highscoreCardController.setImage(highscoreCardStage, cardSettings, display, file);
          root.setCenter(widgetRoot);
        } catch (IOException e) {
          LOG.error("Failed to init dashboard: " + e.getMessage(), e);
        }

        showHighscoreCard(notificationTime);
      } else {
        LOG.info("Skipping highscore card overlay, zero time set.");
      }
    } catch (Exception e) {
      LOG.error("Failed to open highscore card notification: " + e.getMessage());
    }
  }

  private void showHighscoreCard(int notificationTime) {
    highscoreCardStage.show();
    TransitionUtil.createInFader(highscoreCardController.getRoot(), 500).play();
    new Thread(() -> {
      try {
        Thread.sleep(notificationTime * 1000);
      } catch (InterruptedException e) {
        //ignore
      } finally {
        Platform.runLater(() -> {
          FadeTransition outFader = TransitionUtil.createOutFader(highscoreCardController.getRoot(), 500);
          outFader.setOnFinished(event -> highscoreCardStage.hide());
          outFader.play();
        });
      }
    }).start();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    INSTANCE = this;

    this.stage = primaryStage;
    Platform.setImplicitExit(false);

    root = new BorderPane();
    Screen screen = Screen.getPrimary();
    final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);
    scene.setCursor(Cursor.NONE);

    Rectangle2D bounds = screen.getVisualBounds();
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());

    stage.setScene(scene);
    stage.setFullScreenExitHint("");
    stage.setAlwaysOnTop(true);
    stage.setFullScreen(true);
    stage.getScene().getStylesheets().add(OverlayWindowFX.class.getResource("stylesheet.css").toExternalForm());

    PauseMenu.loadPauseMenu();
    latch.countDown();
  }
}
