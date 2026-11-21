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
import de.mephisto.vpin.restclient.util.OSUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.mephisto.vpin.commons.fx.ServerFX.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static Robot robot;

  private Stage stage;
  private boolean visible = false;
  private boolean test = false;
  private volatile boolean alreadyMuted = false;
  private volatile boolean preparing = false;
  /**
   * Incremented on every show request and exit, a menu whose preparation has been overtaken by an exit is not shown.
   */
  private final AtomicInteger toggleRequest = new AtomicInteger();

  private final List<FrontendScreenAsset> screenAssets = new ArrayList<>();
  private final List<PauseMenuStatusChangeListener> listeners = new ArrayList<>();

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

    GameStatus gameStatus = null;

    gameStatus = new GameStatus();
    gameStatus.setGameId(5305);
    gameStatus.setStarted(OffsetDateTime.now());
    client.getGameStatusService().setGameStatus(gameStatus);

    INSTANCE.togglePauseMenu(gameStatus, true);
  }

  public Stage getStage() {
    return stage;
  }

  public boolean isVisible() {
    return visible;
  }

  public void addListener(PauseMenuStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  public void loadPauseMenu() {
    try {
      if (GraphicsEnvironment.isHeadless()) {
        return;
      }

      Stage pauseMenuStage = new Stage();
      pauseMenuStage.setTitle("VPin UI");
      pauseMenuStage.initStyle(StageStyle.TRANSPARENT);
      pauseMenuStage.setAlwaysOnTop(true);
      stage = pauseMenuStage;

      if (!OSUtil.isMac()) {//Let MacOS handle this to use dynamic icons
        stage.getIcons().add(new Image(Objects.requireNonNull(PauseMenu.class.getResourceAsStream("logo-64.png"))));
      }
      PauseMenuSettings pauseMenuSettings = ServerFX.client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
      int pauseMenuScreenId = pauseMenuSettings.getPauseMenuScreenId();

      Optional<MonitorInfo> first = MonitorInfoUtil.getMonitors().stream().filter(m -> m.getId() == pauseMenuScreenId).findFirst();
      MonitorInfo monitorInfo = first.orElseGet(MonitorInfoUtil::getPrimaryMonitor);
      PauseMenuUIDefaults.init(monitorInfo);

      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      loader.setResources(Messages.getBundle());
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

    // Use JavaFX Screen bounds for positioning to avoid AWT/JNA vs JavaFX coordinate system mismatches
    // (AWT getBounds() and JNA EnumDisplayMonitors use different DPI reference scales than JavaFX Stage.setX())
    Screen targetScreen = Screen.getScreens().stream()
        .min(Comparator.comparingDouble(s ->
            Math.pow(s.getBounds().getMinX() - monitorInfo.getScaledX(), 2) +
            Math.pow(s.getBounds().getMinY() - monitorInfo.getMinY(), 2)))
        .orElse(Screen.getPrimary());

    PauseMenuUIDefaults.init(targetScreen);

    double screenWidth = targetScreen.getBounds().getWidth();
    double screenHeight = targetScreen.getBounds().getHeight();
    double screenX = targetScreen.getBounds().getMinX();
    double screenY = targetScreen.getBounds().getMinY();

    double maxWidth = Math.max(screenWidth, screenHeight);
    Scene scene = new Scene(rootPane, screenWidth, screenHeight);

    stage.setY(screenY);

    if (pauseMenuSettings.getRotation() == 90 || pauseMenuSettings.getRotation() == 270) {
      LOG.info("Window Mode: Cab"); //scaling is ignored here!!!
      rootPane.setRotate(-(pauseMenuSettings.getRotation()));
      stage.setX(screenX + screenWidth / 2 / 2);

      if(pauseMenuSettings.isAutoScale()) {
        if (maxWidth > 2560) {
          scaling = 1.4;
        }
        else if (maxWidth > 2000) {
          scaling = 0.9;
        }
        else {
          //falls down too much
          stage.setX(screenX + screenWidth / 2 / 2 / 2);
          scaling = 0.7;
        }
      }
    }
    else {
      LOG.info("Window Mode: Desktop");
      rootPane.setRotate(pauseMenuSettings.getRotation());
      stage.setX(screenX);

      if(pauseMenuSettings.isAutoScale()) {
        if (screenHeight == 1440) {
          scaling = 0.9;
        }
        else if (maxWidth > 2560) {
          rootPane.setTranslateY(500);
          scaling = 1.4;
        }
        else if (maxWidth > 2000) {
          scaling = 1;
        }
        else if (maxWidth > 1600) {
          scaling = 0.75;
        }
        else if (maxWidth > 1200) {
          scaling = 0.6;
        }
        else {
          //falls down too much
          scaling = 0.65;
          if (Screen.getScreens().size() > 1) {
            stage.setY(Screen.getScreens().get(1).getBounds().getMinY());
          }
        }
      }
    }

    int scalingSetting = pauseMenuSettings.getScaling();
    if (scalingSetting != 0 && !pauseMenuSettings.isAutoScale()) {
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

    if (pauseMenuSettings.isApronMode()) {
      stage.setHeight(monitorInfo.getHeight() + Math.abs(pauseMenuSettings.getStageOffsetY()));
      stage.setWidth(monitorInfo.getWidth() + Math.abs(pauseMenuSettings.getStageOffsetX()));
    }

    LOG.info("Showing pause menu: X={}, Y={}, Width={}, Height={}, Scaling={}", stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), rootPane.getScaleX() + "/" + rootPane.getScaleY());
    return scene;
  }

  public void togglePauseMenu() {
    togglePauseMenu(null, false);
  }

  /**
   * Data of the pause menu that is fetched from the server before the menu is shown.
   */
  private record PreparedPauseMenu(@NonNull GameRepresentation game, @NonNull PauseMenuSettings settings,
                                   boolean scoreSubmitterEnabled,
                                   @Nullable FrontendPlayerDisplay tutorialDisplay,
                                   @Nullable FrontendMediaItemRepresentation tutorialItem) {
  }

  /**
   * Must be called on the FX thread. All REST calls and the screenshot are done in the background: blocking the
   * FX thread made the menu laggy, so only the stage handling happens on the FX thread.
   */
  public void togglePauseMenu(@Nullable GameStatus status, boolean test) {
    if (visible) {
      exitPauseMenu();
      return;
    }
    if (preparing) {
      LOG.info("Pause menu is still being prepared, ignoring toggle request.");
      return;
    }

    this.test = test;
    if (test) {
      loadPauseMenu();
    }

    preparing = true;
    int request = toggleRequest.incrementAndGet();
    JFXFuture.supplyAsync(() -> preparePauseMenu(status, test))
        .thenAcceptLater(prepared -> {
          preparing = false;
          if (prepared == null) {
            return;
          }
          //the menu has been exited (e.g. by a table exit) while it was prepared
          if (request != toggleRequest.get()) {
            LOG.info("Skipped showing pause menu, it has been closed during its preparation.");
            if (!test) {
              new Thread(() -> client.getGameStatusService().finishPause(), "Pause Menu Exit").start();
            }
            return;
          }
          showPauseMenu(prepared);
        })
        .onErrorLater(e -> {
          preparing = false;
          LOG.error("Failed to init pause menu: " + e.getMessage(), e);
        });
  }

  /**
   * Runs in the background, everything blocking happens here.
   */
  @Nullable
  private PreparedPauseMenu preparePauseMenu(@Nullable GameStatus status, boolean test) {
    client.getPreferenceService().clearCache();
    if (!test) {
      status = client.getGameStatusService().startPause();
    }

    if (status == null) {
      status = client.getGameStatusService().getStatus();
    }
    if (!status.isActive()) {
      LOG.info("Skipped showing start menu: no game status found.");
      return null;
    }
    LOG.info("Found game status for " + status.getGameId());
    SLOG.info("Found game status for " + status.getGameId());

    //reload card settings to resolve actual target screen
    GameRepresentation game = client.getGameService().getGame(status.getGameId());
    PauseMenuSettings pauseMenuSettings = client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    if (!test && pauseMenuSettings.isPressPause()) {
      togglePauseKey(0);
    }

    FrontendPlayerDisplay tutorialDisplay = null;
    FrontendMediaItemRepresentation tutorialItem = null;
    if (pauseMenuSettings.isTutorialsOnScreen() && pauseMenuSettings.isShowTutorials()) {
      List<VpsTutorialUrls> videoTutorials = PauseMenuItemsFactory.getVideoTutorials(game, pauseMenuSettings);
      if (!videoTutorials.isEmpty()) {
        VPinScreen tutorialScreen = pauseMenuSettings.getTutorialsScreen();
        tutorialDisplay = client.getFrontendService().getScreenDisplay(tutorialScreen);
        tutorialItem = new FrontendMediaItemRepresentation();
        tutorialItem.setMimeType("video/mp4");
        tutorialItem.setScreen(tutorialScreen);
        tutorialItem.setUri(PauseMenuItemsFactory.createVideoUrl(game));
      }
    }

    //we need to take the screenshot before the menu is shown
    boolean scoreSubmitterEnabled = client.getCompetitionService().isScoreSubmitterEnabled();
    if (scoreSubmitterEnabled || pauseMenuSettings.isAlwaysTakeScreenshot()) {
      client.takeScoreScreenshot();
    }
    return new PreparedPauseMenu(game, pauseMenuSettings, scoreSubmitterEnabled, tutorialDisplay, tutorialItem);
  }

  /**
   * Runs on the FX thread.
   */
  private void showPauseMenu(@NonNull PreparedPauseMenu prepared) {
    PauseMenuSettings pauseMenuSettings = prepared.settings();
    try {
      screenAssets.clear();
      if (prepared.tutorialItem() != null) {
        FrontendScreenAsset assetScreen = PauseMenuScreensFactory.createScreenStage(client, prepared.game(), prepared.tutorialDisplay(),
            prepared.tutorialItem().getScreen(), prepared.tutorialItem(), pauseMenuSettings);
        screenAssets.add(assetScreen);
      }

      StateMananger.getInstance().setControls(pauseMenuSettings);

      visible = true;

      PauseMenuState state = new PauseMenuState();
      state.setGame(prepared.game());
      state.setScoreSubmitterEnabled(prepared.scoreSubmitterEnabled());
      state.setApronMode(pauseMenuSettings.isApronMode());
      state.setVisibleItemCount(pauseMenuSettings.getVisibleItemCount());

      StateMananger.getInstance().setState(state);
      stage.getScene().setCursor(Cursor.NONE);

      ServerFX.forceShow(stage);
      LOG.info("Forced showing pause stage, starting post launch processing.");
      JFXFuture.runAsync(() -> {
        Boolean isMuted = WindowsVolumeControl.isMuted();
        alreadyMuted = isMuted != null && isMuted;

        if (pauseMenuSettings.isMuteOnPause() && !alreadyMuted) {
          NirCmd.muteSystem(true);
        }

        listeners.forEach(PauseMenuStatusChangeListener::pauseMenuShow);
        LOG.info("Pause menu screens preparation finished, using {} screen assets.", screenAssets.size());
      });
    }
    catch (Exception e) {
      LOG.error("Failed to init pause menu: " + e.getMessage(), e);
    }
  }

  /**
   * Must be called on the FX thread. Hides the stages immediately, everything blocking (REST calls, NirCmd,
   * listeners) is done in the background.
   */
  public void exitPauseMenu() {
    //cancels a pause menu that is still being prepared
    toggleRequest.incrementAndGet();

    boolean wasVisible = visible;
    boolean wasTest = test;
    boolean wasMuted = alreadyMuted;
    visible = false;

    StateMananger.getInstance().exit();

    LOG.info("Exited pause menu");
    SLOG.info("Exited pause menu");
    if (stage != null) {
      //null for headless tests
      stage.hide();
    }

    List<FrontendScreenAsset> assets = new ArrayList<>(screenAssets);
    screenAssets.clear();
    if (!assets.isEmpty()) {
      PauseTransition delay = new PauseTransition(Duration.millis(PauseMenuUIDefaults.SELECTION_SCALE_DURATION));
      delay.setOnFinished(e -> assets.forEach(asset -> {
        asset.getScreenStage().hide();
        asset.getFrontendScreenController().dispose();
      }));
      delay.play();
    }

    new Thread(() -> {
      try {
        if (!wasTest) {
          client.getGameStatusService().finishPause();
        }
        PauseMenuSettings pauseMenuSettings = client.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

        if (!wasTest) {
          NirCmd.focusWindow("Visual Pinball Player");
        }

        if (pauseMenuSettings.isMuteOnPause() && !wasMuted) {
          NirCmd.muteSystem(false);
        }

        if (wasVisible && pauseMenuSettings.isPressPause() && !wasTest) {
          long delay = 1000;
          if (pauseMenuSettings.getUnpauseDelay() > 0) {
            delay = pauseMenuSettings.getUnpauseDelay();
          }
          togglePauseKey(delay);
        }

        listeners.forEach(PauseMenuStatusChangeListener::pauseMenuHide);
      }
      catch (Exception e) {
        LOG.error("Failed to finish pause menu exit: {}", e.getMessage(), e);
      }
    }, "Pause Menu Exit").start();
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