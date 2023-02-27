package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
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
import java.util.List;

public class MenuMain extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(MenuMain.class);

  public static VPinStudioClient client;

  private static String updateHost = null;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    MenuMain.client = new VPinStudioClient(updateHost);
    loadUpdater(stage);
  }

  public static void loadUpdater(Stage stage) {
    try {
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("main.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root, 1000, 1000);
      scene.setFill(Color.TRANSPARENT);

      stage.setTitle("VPin Studio Menu");
      stage.setScene(scene);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.setX((screenBounds.getWidth() / 2) - (1000 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (1000 / 2));

      MenuController controller = loader.getController();
      MenuKeyListener listener = new MenuKeyListener(controller);
      scene.setOnKeyPressed(event -> {
        listener.handle(event);
      });
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }
}