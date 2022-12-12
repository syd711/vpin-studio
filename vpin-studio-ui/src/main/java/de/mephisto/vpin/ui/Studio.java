package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.ui.launcher.LauncherController;
import de.mephisto.vpin.ui.util.ResizeHelper;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Studio extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(Studio.class);

  public static Stage stage;

  public static VPinStudioClient client;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;

    //replace the OverlayFX client with the Studio one
    Studio.client = new VPinStudioClient("localhost");
    OverlayWindowFX.client = Studio.client;

    String version = client.version();
    if (!StringUtils.isEmpty(version)) {
      loadStudio(stage, Studio.client);
    }
    else {
      loadLauncher(stage, false);
    }
  }

  public static void loadLauncher(Stage stage, boolean runUpdate) {
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
      controller.setStage(stage, runUpdate);
      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
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
      stage.setTitle("VPin Studio - " + Studio.getVersion());
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
      stage.setScene(scene);
      stage.setResizable(true);
      stage.setMinWidth(1780);
      stage.setMinHeight(1200);
      stage.initStyle(StageStyle.UNDECORATED);

      stage.setX((screenBounds.getWidth() / 2) - (width / 2));
      stage.setY((screenBounds.getHeight() / 2) - (height / 2));
      ResizeHelper.addResizeListener(stage);

      stage.show();
    } catch (IOException e) {
      LOG.error("Failed to load Studio: " + e.getMessage(), e);
    }
  }

//  private void loadSplashScreen() {
//    try {
//      //Load splash screen view FXML
//      StackPane pane = FXMLLoader.load(getClass().getResource(("myAwesomeSplashDesign.fxml")));
//      //Add it to root container (Can be StackPane, AnchorPane etc)
//      root.getChildren().setAll(pane);
//
//      //Load splash screen with fade in effect
//      FadeTransition fadeIn = new FadeTransition(Duration.seconds(3), pane);
//      fadeIn.setFromValue(0);
//      fadeIn.setToValue(1);
//      fadeIn.setCycleCount(1);
//
//      //Finish splash with fade out effect
//      FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), pane);
//      fadeOut.setFromValue(1);
//      fadeOut.setToValue(0);
//      fadeOut.setCycleCount(1);
//
//      fadeIn.play();
//
//      //After fade in, start fade out
//      fadeIn.setOnFinished((e) -> {
//        fadeOut.play();
//      });
//
//      //After fade out, load actual content
//      fadeOut.setOnFinished((e) -> {
//        try {
//          AnchorPane parentContent = FXMLLoader.load(getClass().getResource(("/main.fxml")));
//          root.getChildren().setAll(parentContent);
//        } catch (IOException ex) {
//          Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//      });
//    } catch (IOException ex) {
//      Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//    }
//  }

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