package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.states.StateMananger;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.popper.PinUPControls;
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

import java.util.logging.Level;

import static java.util.logging.Logger.*;

public class PauseMenu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenu.class);

  //do not change this title as it is used in popper as launch parameter

  public static VPinStudioClient client;

  private final static boolean PRODUCTION_USE = true;//!new File("./").getAbsolutePath().contains("workspace");
  private final static boolean TEST_PRODUCTION = false;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    PauseMenu.client = new VPinStudioClient("localhost");
    loadUpdater(stage);
  }

  public static void loadUpdater(Stage stage) {
    try {
      stage.getIcons().add(new Image(PauseMenu.class.getResourceAsStream("logo-64.png")));

      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      Parent root = loader.load();

      Scene scene = null;

      if (PRODUCTION_USE || TEST_PRODUCTION) {
        root.setRotate(-90);
        root.setTranslateY(0);
        root.setTranslateX(0);
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        stage.setY(0);
        stage.setX(screenBounds.getWidth() / 2 / 2);
      }
      else {
        scene = new Scene(root, screenBounds.getWidth(), 1000);
        stage.setX((screenBounds.getWidth() / 2) - (screenBounds.getWidth() / 2));
        stage.setY((screenBounds.getHeight() / 2) - (1000 / 2));
      }

      scene.setFill(Color.TRANSPARENT);
      stage.setTitle(de.mephisto.vpin.commons.fx.UIDefaults.MANAGER_TITLE);
      stage.setScene(scene);
      stage.initStyle(StageStyle.TRANSPARENT);


      MenuController controller = loader.getController();
      StateMananger.getInstance().init(controller);

      PinUPControls pinUPControls = client.getPinUPPopperService().getPinUPControls();
      StateMananger.getInstance().setControls(pinUPControls);

      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);
      GlobalScreen.addNativeKeyListener(StateMananger.getInstance());

      if (PRODUCTION_USE) {
        //TODO execute pause exe here
      }
      stage.show();
    } catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }
}