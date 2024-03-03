package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuScreensFactory;
import de.mephisto.vpin.commons.fx.pausemenu.model.PopperScreenAsset;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
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
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenu.class);

  public static VPinStudioClient client;

  private static boolean PRODUCTION_USE = true;

  public static Stage stage;
  private static boolean visible = false;

  private static Robot robot;

  private static List<PopperScreenAsset> screenAssets = new ArrayList<>();

  static {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      LOG.error("Failed to create robot: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    OverlayWindowFX.client = new VPinStudioClient("localhost");
    PRODUCTION_USE = false;
    launch(args);
    PauseMenu.togglePauseMenu();
  }

  @Override
  public void start(Stage stage) {
    loadPauseMenu();
  }

  public static void loadPauseMenu() {
    Stage pauseMenuStage = new Stage();
    pauseMenuStage.initStyle(StageStyle.TRANSPARENT);
    pauseMenuStage.setAlwaysOnTop(true);
    PauseMenu.stage = pauseMenuStage;

    Scene scene = null;
    client = new VPinStudioClient("localhost");

    try {
      stage.getIcons().add(new Image(PauseMenu.class.getResourceAsStream("logo-64.png")));

      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      BorderPane root = loader.load();

      int height = 1400;
      if (PRODUCTION_USE) {
        if (screenBounds.getWidth() > screenBounds.getHeight()) {
          LOG.info("Window Mode: Landscape");
          root.setTranslateY(0);
          root.setTranslateX(0);
          root.setRotate(-90);
          stage.setY((screenBounds.getHeight() - root.getPrefWidth()) / 2);
          stage.setX(screenBounds.getWidth() / 2 / 2);
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
          stage.setX((screenBounds.getWidth() - root.getPrefWidth()) / 2);
          stage.setY(screenBounds.getHeight() / 2 / 2);
          double max = Math.max(screenBounds.getWidth(), screenBounds.getHeight());
          if (max > 2560) {
            root.setTranslateY(400);
          }
          scene = new Scene(root, root.getPrefWidth(), root.getPrefWidth());
        }
      }
      else {
        scene = new Scene(root, screenBounds.getWidth(), height);
        stage.setX((screenBounds.getWidth() / 2) - (screenBounds.getWidth() / 2));
        stage.setY((screenBounds.getHeight() / 2) - (height / 2));
      }

      scalePauseMenuStage(root, screenBounds);
      scene.setFill(Color.TRANSPARENT);
      stage.setTitle(de.mephisto.vpin.commons.fx.UIDefaults.MANAGER_TITLE);
      stage.setScene(scene);

      StateMananger.getInstance().init(loader.getController());

      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);

      if (!PRODUCTION_USE) {
        togglePauseMenu();
      }
    } catch (Exception e) {
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
    if (!visible) {
      GameStatus status = client.getGameStatusService().getStatus();
      if (!status.isActive()) {
        LOG.info("Skipped showing start menu: no game status found.");
        return;
      }

      togglePauseKey(0);

      //re-assign key, because they might have been changed
      PinUPControls pinUPControls = client.getPinUPPopperService().getPinUPControls();
      StateMananger.getInstance().setControls(pinUPControls);

      //reload card settings to resolve actual target screen
      CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
      PopperScreen screen = null;
      if (!StringUtils.isEmpty(cardSettings.getPopperScreen())) {
        screen = PopperScreen.valueOf(cardSettings.getPopperScreen());
      }

      PinUPPlayerDisplay screenDisplay = client.getPinUPPopperService().getScreenDisplay(PopperScreen.BackGlass);
      PauseMenuSettings pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

      visible = true;
      GameRepresentation game = client.getGameService().getGame(status.getGameId());
      StateMananger.getInstance().setGame(game, status, screen, screenDisplay, pauseMenuSettings);
      stage.getScene().setCursor(Cursor.NONE);
      new Thread(() -> {
        Platform.runLater(() -> {
          screenAssets.clear();
          screenAssets.addAll(PauseMenuScreensFactory.createAssetScreens(game, client, client.getPinUPPopperService().getScreenDisplays()));
        });

        OverlayWindowFX.toFront(stage, visible);
        OverlayWindowFX.toFront(stage, visible);
        OverlayWindowFX.toFront(stage, visible);
        OverlayWindowFX.toFront(stage, visible);
      }).start();
      OverlayWindowFX.forceShow(stage);
    }
    else {
      exitPauseMenu();
    }
  }

  public static void exitPauseMenu() {
    StateMananger.getInstance().exit();
    if (!PRODUCTION_USE) {
      Platform.runLater(() -> {
        System.exit(0);
      });
    }
    else {
      LOG.info("Exited pause menu");
      stage.hide();
      screenAssets.stream().forEach(asset -> {
        asset.getScreenStage().hide();
        asset.dispose();
      });

      try {
        SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("sendKeys.bat", "Visual Pinball Player", ""));
        executor.setDir(new File("./resources"));
        executor.executeCommand();
      } catch (Exception e) {
        LOG.error("Failed to execute focus command: " + e.getMessage(), e);
      }
    }

    if (visible) {
      new Thread(() -> {
        togglePauseKey(1000);
      }).start();
    }
    visible = false;
  }

  private static void togglePauseKey(long delay) {
    try {
      if (!PRODUCTION_USE) {
        return;
      }

      Thread.sleep(delay);
      robot.keyPress(KeyEvent.VK_P);
      Thread.sleep(100);
      robot.keyRelease(KeyEvent.VK_P);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}