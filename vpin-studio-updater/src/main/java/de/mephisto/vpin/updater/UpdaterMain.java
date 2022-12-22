package de.mephisto.vpin.updater;

import de.mephisto.vpin.commons.utils.Updater;
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

public class UpdaterMain extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(UpdaterMain.class);

  public static VPinStudioClient client;

  private static String updateHost = null;

  public static void main(String[] args) throws Exception {
    if (args != null && args.length > 0) {
      if (args[0].equals("-server")) {
        String serverVersion = args[1];
        LOG.info("Starting server update, updating from version " + serverVersion);
        String updateVersion = Updater.checkForUpdate(serverVersion);
        if (updateVersion != null && !updateVersion.equals(serverVersion)) {
          LOG.info("Server updated process found updater " + updateVersion + " and is running on " + serverVersion);
          Updater.updateServer(updateVersion);
          Updater.restartServer();
        }
      }
      else {
        updateHost = args[0];
        launch(args);
      }
    }
    else {
      LOG.error("Updater must be called with arguments.");
    }
  }

  @Override
  public void start(Stage stage) {
    UpdaterMain.client = new VPinStudioClient(updateHost);
    loadUpdater(stage);
  }

  public static void loadUpdater(Stage stage) {
    try {
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(UpdateController.class.getResource("scene-updater.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root, 800, 500);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio Updater");
      stage.getIcons().add(new Image(UpdateController.class.getResourceAsStream("logo-64.png")));
      stage.setScene(scene);
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setX((screenBounds.getWidth() / 2) - (800 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (400 / 2));

      UpdateController controller = loader.getController();
      controller.setStage(stage);
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }
}