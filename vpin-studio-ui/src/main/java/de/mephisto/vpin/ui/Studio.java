package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientErrorHandler;
import de.mephisto.vpin.ui.launcher.LauncherController;
import de.mephisto.vpin.ui.util.FXResizeHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class Studio extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(Studio.class);

  public static Stage stage;

  public static VPinStudioClient client;

  public static void main(String[] args) {
    launch(args);
  }

  private static VPinStudioClientErrorHandler errorHandler;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;
    Locale.setDefault(Locale.ENGLISH);

    Studio.errorHandler = e -> {
      client.setErrorHandler(null);
      Platform.runLater(() -> {
        Studio.stage.close();
        NavigationController.refreshControllerCache();
        NavigationController.refreshViewCache();

        Studio.loadLauncher(new Stage());
        WidgetFactory.showAlert(stage, "Server Connection Failed", "You have been disconnected from the server.");
      });
    };

    //replace the OverlayFX client with the Studio one
    Studio.client = new VPinStudioClient("localhost");
    OverlayWindowFX.client = Studio.client;

    String version = client.getSystemService().getVersion();
    if (!StringUtils.isEmpty(version)) {
      loadStudio(stage, Studio.client);
    }
    else {
      loadLauncher(stage);
    }
  }

  public static void loadLauncher(Stage stage) {
    try {
      Studio.stage = stage;
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(LauncherController.class.getResource("scene-launcher.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root, 800, 500);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio Launcher");
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
      stage.setScene(scene);
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setX((screenBounds.getWidth() / 2) - (800 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (400 / 2));

      LauncherController controller = loader.getController();
      controller.setStage(stage);
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  public static void loadStudio(Stage stage, VPinStudioClient client) {
    try {
      Studio.stage = stage;
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();

      if (screenBounds.getWidth() > screenBounds.getHeight()) {
        LOG.info("Window Mode: Landscape");
      }
      else {
        LOG.info("Window Mode: Portrait");
      }

      //replace the OverlayFX client with the Studio one
      Studio.client = client;
      OverlayWindowFX.client = Studio.client;

      FXMLLoader loader = new FXMLLoader(Studio.class.getResource("scene-root.fxml"));
      Parent root = loader.load();

      double screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
      double width = 1920 * (screenResolution / 100);
      double height = 1080 * (screenResolution / 100);

      if (screenResolution > 100) {
        double screenWidth = screenBounds.getWidth() * (screenResolution / 100);
        if (width > screenWidth) {
          width = screenWidth - (screenWidth * (screenResolution - 100) / 100);
        }

        double screenHeight = screenBounds.getHeight() * (screenResolution / 100);
        if (height > screenHeight) {
          height = screenHeight - (screenHeight * (screenResolution - 100) / 100);
        }
      }

      if (screenBounds.getHeight() > 1280) {
        height = 1300;
      }
      if (screenBounds.getHeight() >= 1480) {
        height = 1400;
      }

      Scene scene = new Scene(root, width, height);
      scene.setFill(Paint.valueOf("#212529"));
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
      stage.setScene(scene);
      stage.setResizable(true);
      stage.setMinWidth(1580);
      stage.setMinHeight(1000);
      stage.initStyle(StageStyle.UNDECORATED);

      stage.setX((screenBounds.getWidth() / 2) - (width / 2));
      stage.setY((screenBounds.getHeight() / 2) - (height / 2));
//      ResizeHelper.addResizeListener(stage);
      FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
      stage.setUserData(fxResizeHelper);

      client.setErrorHandler(errorHandler);

      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load Studio: " + e.getMessage(), e);
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
}