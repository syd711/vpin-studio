package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.logging.Level;

import static java.util.logging.Logger.*;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenu.class);

  //do not change this title as it is used in popper as launch parameter

  public static VPinStudioClient client;

  private final static boolean PRODUCTION_USE = !new File("./").getAbsolutePath().contains("workspace");

  private static Stage stage;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    PauseMenu.client = new VPinStudioClient("localhost");
    loadUpdater(stage);
  }

  public static void loadUpdater(Stage stage) {
    PauseMenu.stage = stage;
    try {
      stage.getIcons().add(new Image(PauseMenu.class.getResourceAsStream("logo-64.png")));

      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      Parent root = loader.load();

      Scene scene = null;

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


      MenuController controller = loader.getController();

      PinUPControls pinUPControls = client.getPinUPPopperService().getPinUPControls();
      StateMananger.getInstance().setControls(pinUPControls);

      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);

      CardSettings cardSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
      PopperScreen screen = PopperScreen.valueOf(cardSettings.getPopperScreen());
      if (PRODUCTION_USE) {
        //TODO execute pause exe here
      }
      else {
        GlobalScreen.addNativeKeyListener(StateMananger.getInstance());
        GameRepresentation game = client.getGameService().getGame(243);
        controller.setGame(game, screen);
        stage.show();
      }

      StateMananger.getInstance().init(controller);
    } catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  public static void exit() {
    if(!PRODUCTION_USE) {
      System.exit(0);
    }
    else {
      GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
      stage.hide();
    }
  }
}