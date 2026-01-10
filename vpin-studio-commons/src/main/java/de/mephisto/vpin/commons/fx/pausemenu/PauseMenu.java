package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.MonitorInfoUtil;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemsFactory;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuScreensFactory;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuState;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.commons.utils.WindowsVolumeControl;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static de.mephisto.vpin.commons.fx.ServerFX.client;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static Robot robot;

  private Stage stage;
  private boolean visible = false;
  private boolean test = false;
  private boolean alreadyMuted = false;

  private final List<FrontendScreenAsset> screenAssets = new ArrayList<>();

  private static PauseMenu INSTANCE = null;
  private MenuController menuController;


  public static PauseMenu getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PauseMenu();
    }
    return INSTANCE;
  }

  static {
    try {
      robot = new Robot();
    }
    catch (AWTException e) {
      LOG.error("Failed to create robot: {}", e.getMessage());
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    ServerFX.client = new VPinStudioClient("localhost");
    INSTANCE = new PauseMenu();
    INSTANCE.loadPauseMenu();
    INSTANCE.togglePauseMenu(null, true);
  }

  public Stage getStage() {
    return stage;
  }

  public boolean isVisible() {
    return visible;
  }

  public void loadPauseMenu() {
    try {
      Stage pauseMenuStage = new Stage();
      pauseMenuStage.setTitle("VPin UI");
      pauseMenuStage.initStyle(StageStyle.TRANSPARENT);
      pauseMenuStage.setAlwaysOnTop(true);
      stage = pauseMenuStage;

      stage.getIcons().add(new Image(Objects.requireNonNull(PauseMenu.class.getResourceAsStream("logo-64.png"))));

      PauseMenuSettings pauseMenuSettings = ServerFX.client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
      int pauseMenuScreenId = pauseMenuSettings.getPauseMenuScreenId();

      Optional<MonitorInfo> first = MonitorInfoUtil.getMonitors().stream().filter(m -> m.getId() == pauseMenuScreenId).findFirst();
      MonitorInfo monitorInfo = first.orElseGet(MonitorInfoUtil::getPrimaryMonitor);
      PauseMenuUIDefaults.init(monitorInfo);

      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      BorderPane rootPane = loader.load();
      menuController = loader.getController();

      Scene scene = scalePauseMenuStage(monitorInfo, rootPane, pauseMenuSettings);
      scene.setFill(Color.TRANSPARENT);
      stage.setScene(scene);

      StateMananger.getInstance().init(menuController);
    }
    catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  private Scene scalePauseMenuStage(MonitorInfo monitorInfo, BorderPane rootPane, PauseMenuSettings pauseMenuSettings) {
    double scaling = 1;
    double max = Math.max(PauseMenuUIDefaults.getScreenWidth(), PauseMenuUIDefaults.getScreenHeight());
    Scene scene = new Scene(rootPane, PauseMenuUIDefaults.getScreenWidth(), PauseMenuUIDefaults.getScreenHeight());

    stage.setY(monitorInfo.getMinY());

    if (pauseMenuSettings.getRotation() != 0) {
      LOG.info("Window Mode: Cab"); //scaling is ignored here!!!
      rootPane.setRotate(-(pauseMenuSettings.getRotation()));
      stage.setX(PauseMenuUIDefaults.getScaledScreenX() + PauseMenuUIDefaults.getScreenWidth() / 2 / 2);

      if (max > 2560) {
        scaling = 1.4;
      }
      else if (max > 2000) {
        scaling = 0.9;
      }
      else {
        //falls down too much
        stage.setX(PauseMenuUIDefaults.getScaledScreenX() + PauseMenuUIDefaults.getScreenWidth() / 2 / 2 / 2);
        scaling = 0.7;
      }
    }
    else {
      LOG.info("Window Mode: Desktop");
      stage.setX(PauseMenuUIDefaults.getScaledScreenX());

      double screenHeight = PauseMenuUIDefaults.getScreenHeight();
      if (screenHeight == 1440) {
        scaling = 0.9;
      }
      else if (max > 2560) {
        rootPane.setTranslateY(500);
        scaling = 1.4;
      }
      else if (max > 2000) {
        scaling = 1;
      }
      else if (max > 1200) {
        scaling = 0.9;
      }
      else {
        //falls down too much
        scaling = 0.65;
        stage.setY(Screen.getScreens().get(1).getBounds().getMinY());
      }
    }

    int scalingSetting = pauseMenuSettings.getScaling();
    if (scalingSetting != 0) {
      scaling = (double) scalingSetting / 100;
      rootPane.setScaleX(scaling);
      rootPane.setScaleY(scaling);
    }
    else {
      rootPane.setScaleX(scaling);
      rootPane.setScaleY(scaling);
    }

    stage.setX(stage.getX() + pauseMenuSettings.getStageOffsetX());
    stage.setY(stage.getY() + pauseMenuSettings.getStageOffsetY());

    return scene;
  }

  public void togglePauseMenu() {
    togglePauseMenu(null, false);
  }

  public void togglePauseMenu(@Nullable GameStatus status, boolean test) {
    this.test = test;
    client.getPreferenceService().clearCache();

    if (!visible) {
      if (!test) {
        status = client.getGameStatusService().startPause();
      }
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

        //reload card settings to resolve actual target screen
        GameRepresentation game = client.getGameService().getGame(status.getGameId());
        PauseMenuSettings pauseMenuSettings = client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

        if (!test && pauseMenuSettings.isPressPause()) {
          togglePauseKey(0);
        }

        screenAssets.clear();
        if (pauseMenuSettings.isTutorialsOnScreen() && pauseMenuSettings.isShowTutorials()) {
          VPinScreen tutorialScreen = pauseMenuSettings.getTutorialsScreen();
          List<VpsTutorialUrls> videoTutorials = PauseMenuItemsFactory.getVideoTutorials(game, pauseMenuSettings);
          if (!videoTutorials.isEmpty()) {
            String videoUrl = PauseMenuItemsFactory.createVideoUrl(game);
            VPinScreen tutorialsScreen = pauseMenuSettings.getTutorialsScreen();
            FrontendPlayerDisplay screenDisplay = client.getFrontendService().getScreenDisplay(tutorialsScreen);
            FrontendMediaItemRepresentation tutorialItem = new FrontendMediaItemRepresentation();
            tutorialItem.setMimeType("video/mp4");
            tutorialItem.setScreen(tutorialScreen);
            tutorialItem.setGameId(game.getId());
            tutorialItem.setUri(videoUrl);

            FrontendScreenAsset assetScreen = PauseMenuScreensFactory.createScreenStage(client, game, screenDisplay, tutorialScreen, tutorialItem, pauseMenuSettings);
            screenAssets.add(assetScreen);
          }
        }

        StateMananger.getInstance().setControls(pauseMenuSettings);

        //we need to take the screenshot before the menu is shown
        boolean scoreSubmitterEnabled = client.getCompetitionService().isScoreSubmitterEnabled();
        if (!test & scoreSubmitterEnabled) {
          client.takeScreenshot();
        }

        visible = true;

        PauseMenuState state = new PauseMenuState();
        state.setGame(game);
        state.setScoreSubmitterEnabled(scoreSubmitterEnabled);
        state.setApronMode(pauseMenuSettings.isApronMode());

        StateMananger.getInstance().setState(state);
        stage.getScene().setCursor(Cursor.NONE);

        ServerFX.forceShow(stage);
        LOG.info("Forced showing pause stage, starting post launch processing.");
        JFXFuture.supplyAsync(() -> {
          Boolean isMuted = WindowsVolumeControl.isMuted();
          alreadyMuted = isMuted != null && isMuted;

          if (pauseMenuSettings.isMuteOnPause() && !alreadyMuted) {
            NirCmd.muteSystem(true);
          }
          return true;
        }).thenAcceptLater((result) -> {
          long start = System.currentTimeMillis();
          try {
            LOG.info("Pause menu screens preparation finished, using {} screen assets.", screenAssets.size());
          }
          catch (Exception e) {
            LOG.error("Failed to prepare pause menu screens: {}", e.getMessage(), e);
          }

          LOG.info("Asset fetch for pause menu took {}ms", (System.currentTimeMillis() - start));
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

  public void exitPauseMenu() {
    if (!test) {
      client.getGameStatusService().finishPause();
    }

    StateMananger.getInstance().exit();
    PauseMenuSettings pauseMenuSettings = client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    LOG.info("Exited pause menu");
    SLOG.info("Exited pause menu");
    stage.hide();

    Platform.runLater(() -> {
      try {
        Thread.sleep(PauseMenuUIDefaults.SELECTION_SCALE_DURATION);
      }
      catch (InterruptedException e) {
        //
      }
      screenAssets.stream().forEach(asset -> {
        asset.getScreenStage().hide();
        asset.dispose();
      });
      screenAssets.clear();
    });


    try {
      if (!test) {
        NirCmd.focusWindow("Visual Pinball Player");
      }

      if (pauseMenuSettings.isMuteOnPause() && !alreadyMuted) {
        NirCmd.muteSystem(false);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to execute focus command: {}", e.getMessage(), e);
    }

    if (visible && pauseMenuSettings.isPressPause()) {
      new Thread(() -> {
        long delay = 1000;
        if (pauseMenuSettings.getUnpauseDelay() > 0) {
          delay = pauseMenuSettings.getUnpauseDelay();
        }
        if (!test) {
          togglePauseKey(delay);
        }
      }).start();
    }
    visible = false;
  }

  private static void togglePauseKey(long delay) {
    new Thread(() -> {
      try {
        Thread.sleep(delay);
        robot.keyPress(KeyEvent.VK_P);
        Thread.sleep(100);
        robot.keyRelease(KeyEvent.VK_P);
        LOG.info("Sending Pause key 'P'");
      }
      catch (Exception e) {
        LOG.error("Failed sending pause key toggle: {}", e.getMessage(), e);
      }
    }).start();
  }
}