package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

import static java.util.logging.Logger.getLogger;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenu.class);

  //do not change this title as it is used in popper as launch parameter

  public static VPinStudioClient client;

  private static boolean PRODUCTION_USE = true;

  private static Stage stage;
  private static MenuController controller;

  public static void main(String[] args) {
    OverlayWindowFX.client = new VPinStudioClient("localhost");
    PRODUCTION_USE = false;
    launch(args);
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
      Parent root = loader.load();

      int height = 1400;
      if (PRODUCTION_USE) {
        root.setRotate(-90);
        root.setTranslateY(0);
        root.setTranslateX(0);
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        stage.setY(0);
        stage.setX(screenBounds.getWidth() / 2 / 2);
      }
      else {
        scene = new Scene(root, screenBounds.getWidth(), height);
        stage.setX((screenBounds.getWidth() / 2) - (screenBounds.getWidth() / 2));
        stage.setY((screenBounds.getHeight() / 2) - (height / 2));
      }

      scene.setFill(Color.TRANSPARENT);
      stage.setTitle(de.mephisto.vpin.commons.fx.UIDefaults.MANAGER_TITLE);
      stage.setScene(scene);
      stage.initStyle(StageStyle.TRANSPARENT);

      controller = loader.getController();
      StateMananger.getInstance().init(controller);

      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);
    } catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  public static void showPauseMenu() {
    //re-assign key, because they might have been changed
    PinUPControls pinUPControls = client.getPinUPPopperService().getPinUPControls();
    StateMananger.getInstance().setControls(pinUPControls);

    //reload card settings to resolve actual target screen
    CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    PopperScreen screen = PopperScreen.valueOf(cardSettings.getPopperScreen());

    GameStatus status = client.getGameStatusService().getStatus();
    GameRepresentation game = client.getGameService().getGame(status.getGameId());
    controller.setGame(game, status, screen);

    GlobalScreen.addNativeKeyListener(StateMananger.getInstance());
    stage.show();
  }

  public static void exit() {
    GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
    if (!PRODUCTION_USE) {
      System.exit(0);
    }
    else {
      stage.hide();
    }
  }
}