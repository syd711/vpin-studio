package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuScreensFactory;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults.SELECTION_SCALE_DURATION;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenu.class);

  public static VPinStudioClient client;

  public static Stage stage;
  public static boolean visible = false;

  private static Robot robot;
  private static boolean test = false;

  private static List<FrontendScreenAsset> screenAssets = new ArrayList<>();
  private static GameEmulatorRepresentation emulator;

  static {
    try {
      robot = new Robot();
    }
    catch (AWTException e) {
      LOG.error("Failed to create robot: " + e.getMessage());
    }
  }


  @Override
  public void start(Stage stage) {
    loadPauseMenu();
  }

  public static boolean isVisible() {
    return visible;
  }

  public static void loadPauseMenu() {
    try {
      Stage pauseMenuStage = new Stage();
      pauseMenuStage.setTitle("VPin UI");
      pauseMenuStage.initStyle(StageStyle.TRANSPARENT);
      pauseMenuStage.setAlwaysOnTop(true);
      PauseMenu.stage = pauseMenuStage;

      Scene scene = null;
      client = new VPinStudioClient("localhost");

      stage.getIcons().add(new Image(PauseMenu.class.getResourceAsStream("logo-64.png")));

      PauseMenuSettings pauseMenuSettings = ServerFX.client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
      Screen playfieldScreen = SystemUtil.getScreenById(pauseMenuSettings.getPauseMenuScreenId());
      LOG.info("Pause Menu is using screen {}", playfieldScreen);
      Rectangle2D screenBounds = playfieldScreen.getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      BorderPane root = loader.load();

      if (screenBounds.getWidth() > screenBounds.getHeight()) {
        LOG.info("Window Mode: Landscape");
        root.setTranslateY(0);
        root.setTranslateX(0);
        root.setRotate(-90);
        stage.setY((screenBounds.getHeight() - root.getPrefWidth()) / 2);
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() / 2 / 2));
        double max = Math.max(screenBounds.getWidth(), screenBounds.getHeight());
        if (max > 2560) {
          stage.setX(stage.getX() + 600);
          root.setTranslateX(400);
        }
        else if (max > 2000) {
          root.setTranslateX(400);
        }
        scene = new Scene(root, root.getPrefWidth(), root.getPrefWidth());
      }
      else {
        LOG.info("Window Mode: Portrait");
        root.setTranslateY(0);
        root.setTranslateX(0);
        stage.setX(screenBounds.getMinX() + ((screenBounds.getWidth() - root.getPrefWidth()) / 2));
        stage.setY(screenBounds.getHeight() / 2 / 2);
        double max = Math.max(screenBounds.getWidth(), screenBounds.getHeight());
        if (max > 2560) {
          root.setTranslateY(400);
        }
        scene = new Scene(root, root.getPrefWidth(), root.getPrefWidth());
      }

      scalePauseMenuStage(root, screenBounds);
      scene.setFill(Color.TRANSPARENT);
      stage.setScene(scene);

      StateMananger.getInstance().init(loader.getController());
    }
    catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  private static void scalePauseMenuStage(BorderPane root, Rectangle2D screenBounds) {
    double max = Math.max(screenBounds.getWidth(), screenBounds.getHeight());
    double scaling = 1;
    if (max > 2560) {
      scaling = 1.4;
    }
    else if (max < 2000) {
      scaling = 0.7;
    }
    root.setScaleX(scaling);
    root.setScaleY(scaling);
  }

  public static void togglePauseMenu() {
    togglePauseMenu(null, false);
  }

  public static void togglePauseMenu(@Nullable GameStatus status, boolean test) {
    client.getPreferenceService().clearCache();
    PauseMenu.test = test;

    if (!visible) {
      try {
        if (status == null) {
          status = client.getGameStatusService().getStatus();
        }
        if (!status.isActive()) {
          LOG.info("Skipped showing start menu: no game status found.");
          return;
        }
        else {
          LOG.info("Found game status for " + status.getGameId());
          SLOG.info("Found game status for " + status.getGameId());
        }

        togglePauseKey(0);

        //reload card settings to resolve actual target screen
        CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
        PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

        GameRepresentation game = client.getGameService().getGame(status.getGameId());
        emulator = client.getEmulatorService().getGameEmulator(game.getEmulatorId());

        StateMananger.getInstance().setControls(pauseMenuSettings);

        VPinScreen cardScreen = null;
        if (!StringUtils.isEmpty(cardSettings.getPopperScreen())) {
          cardScreen = VPinScreen.valueOf(cardSettings.getPopperScreen());
        }

        VPinScreen tutorialScreen = VPinScreen.BackGlass;
        if (pauseMenuSettings.getVideoScreen() != null) {
          tutorialScreen = pauseMenuSettings.getVideoScreen();
        }

        FrontendPlayerDisplay tutorialDisplay = client.getFrontendService().getScreenDisplay(tutorialScreen);

        visible = true;
        FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());

        String extTableId = game.getExtTableId();
        VpsTable tableById = client.getVpsService().getTableById(extTableId);

        StateMananger.getInstance().setGame(game, frontendMedia, status, tableById, cardScreen, tutorialDisplay, pauseMenuSettings);
        stage.getScene().setCursor(Cursor.NONE);

        JFXFuture.supplyAsync(() -> {
          if (pauseMenuSettings.isMuteOnPause()) {
            NirCmd.muteSystem(true);
          }
          return true;
        }).thenAcceptLater((result) -> {
          long start = System.currentTimeMillis();
          try {
            screenAssets.clear();
            screenAssets.addAll(PauseMenuScreensFactory.createAssetScreens(game, client, frontendMedia));
            LOG.info("Pause menu screens preparation finished, using " + screenAssets.size() + " screen assets.");
          }
          catch (Exception e) {
            LOG.error("Failed to prepare pause menu screens: " + e.getMessage(), e);
          }

          LOG.info("Asset fetch for pause menu took {}ms", (System.currentTimeMillis() - start));

          ServerFX.forceShow(stage);
          if (emulator != null && isVPXGlEmulator(emulator)) {
//            ServerFX.toFront(stage, visible);
//            ServerFX.toFront(stage, visible);
//            ServerFX.toFront(stage, visible);
//            ServerFX.toFront(stage, visible);
          }
        });
      }
      catch (Exception e) {
        LOG.error("Failed to init pause menu: " + e.getMessage(), e);
      }
    }
    else {
      exitPauseMenu();
    }
  }

  private static boolean isVPXGlEmulator(GameEmulatorRepresentation emulator) {
    String name = String.valueOf(emulator.getName());
    String desc = String.valueOf(emulator.getDescription());
    return name.contains("GL") || desc.contains("GL");
  }

  public static void exitPauseMenu() {
    StateMananger.getInstance().exit();
    PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    LOG.info("Exited pause menu");
    SLOG.info("Exited pause menu");
    stage.hide();

    Platform.runLater(() -> {
      try {
        Thread.sleep(SELECTION_SCALE_DURATION);
      }
      catch (InterruptedException e) {
        //
      }
      screenAssets.stream().forEach(asset -> {
        asset.getScreenStage().hide();
        asset.dispose();
      });
    });


    try {
      NirCmd.focusWindow("Visual Pinball Player");
      if (pauseMenuSettings.isMuteOnPause()) {
        NirCmd.muteSystem(false);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to execute focus command: " + e.getMessage(), e);
    }

    if (visible) {
      new Thread(() -> {
        long delay = 1000;
        if (pauseMenuSettings.getUnpauseDelay() > 0) {
          delay = pauseMenuSettings.getUnpauseDelay();
        }
        togglePauseKey(delay);
      }).start();
    }
    visible = false;
  }

  private static void togglePauseKey(long delay) {
    try {
      if (test) {
        return;
      }

      Thread.sleep(delay);
      robot.keyPress(KeyEvent.VK_P);
      Thread.sleep(100);
      robot.keyRelease(KeyEvent.VK_P);
      LOG.info("Sending Pause key 'P'");
    }
    catch (Exception e) {
      LOG.error("Failed sending pause key toggle: " + e.getMessage(), e);
    }
  }
}