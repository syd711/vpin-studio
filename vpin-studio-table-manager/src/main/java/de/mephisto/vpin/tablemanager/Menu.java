package de.mephisto.vpin.tablemanager;

import de.mephisto.vpin.tablemanager.states.StateMananger;
import de.mephisto.vpin.restclient.VPinStudioClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Menu extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(Menu.class);

  //do not change this title as it is used in popper as launch parameter
  public static final String TITLE = "VPin Studio Table Manager";

  public static VPinStudioClient client;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    Menu.client = new VPinStudioClient("localhost");
    loadUpdater(stage);
  }

  public static void loadUpdater(Stage stage) {
    try {
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("menu-main.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root, UIDefaults.SCREEN_WIDTH, 1000);
      scene.setFill(Color.TRANSPARENT);

      stage.setTitle(TITLE);
      stage.setScene(scene);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.setX((screenBounds.getWidth() / 2) - (UIDefaults.SCREEN_WIDTH / 2));
      stage.setY((screenBounds.getHeight() / 2) - (1000 / 2));

      MenuController controller = loader.getController();
      StateMananger.getInstance().init(controller);
      scene.setOnKeyReleased(event -> {
        StateMananger.getInstance().handle(event);
      });
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }
}