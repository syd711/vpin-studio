package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.ui.launcher.LauncherController;
import de.mephisto.vpin.ui.util.ResizeHelper;
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
import java.io.InputStream;
import java.util.Properties;

public class Studio extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(Studio.class);

  public static Stage stage;

  public static VPinStudioClient client;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;

    //replace the OverlayFX client with the Studio one
    Studio.client = new VPinStudioClient("localhost");
    OverlayWindowFX.client = Studio.client;

    boolean localPingResult = false; //client.ping();
    if (localPingResult) {
      loadStudio(stage, Studio.client);
    }
    else {
      loadLauncher(stage);
    }
  }

  public static void loadLauncher(Stage stage) {
    try {
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(LauncherController.class.getResource("scene-launcher.fxml"));
      Parent root = loader.load();
      LauncherController controller = loader.getController();
      controller.setStage(stage);

      Scene scene = new Scene(root, 800, 400);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio Launcher");
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
      stage.setScene(scene);
      stage.setResizable(false);
      stage.setX((screenBounds.getWidth() / 2) - (800 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (400 / 2));
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = Studio.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    } catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }

  public static void loadStudio(Stage stage, VPinStudioClient client) {
    try {
      Studio.stage = stage;
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();

      //replace the OverlayFX client with the Studio one
      Studio.client = client;
      OverlayWindowFX.client = Studio.client;

      FXMLLoader loader = new FXMLLoader(Studio.class.getResource("scene-root.fxml"));
      Parent root = loader.load();

      int width = 1920;
      int height = 1080;
      if (screenBounds.getHeight() >= 1280) {
        height = 1200;
      }
      if (screenBounds.getHeight() >= 1480) {
        height = 1400;
      }

      Scene scene = new Scene(root, width, height);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio");
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
      stage.setScene(scene);
      stage.setResizable(true);
      stage.initStyle(StageStyle.UNDECORATED);

      stage.setX((screenBounds.getWidth() / 2) - (width / 2));
      stage.setY((screenBounds.getHeight() / 2) - (height / 2));
      ResizeHelper.addResizeListener(stage, width, 1080, width * 2, height * 2);

      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load Studio: " + e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    launch();
  }
}