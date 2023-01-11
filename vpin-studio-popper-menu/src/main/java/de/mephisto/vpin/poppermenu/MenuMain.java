package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.restclient.VPinStudioClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MenuMain extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(MenuMain.class);

  public static VPinStudioClient client;

  private static String updateHost = null;

  public static void main(String[] args) throws Exception {

  }

  @Override
  public void start(Stage stage) {
    MenuMain.client = new VPinStudioClient(updateHost);
    loadUpdater(stage);
  }

  public static void loadUpdater(Stage stage) {
    try {
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(MenuController.class.getResource("scene-updater.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root, 800, 500);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio Menu");
      stage.getIcons().add(new Image(MenuController.class.getResourceAsStream("logo-64.png")));
      stage.setScene(scene);
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setX((screenBounds.getWidth() / 2) - (800 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (400 / 2));

      MenuController controller = loader.getController();
      controller.setStage(stage);
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }
}