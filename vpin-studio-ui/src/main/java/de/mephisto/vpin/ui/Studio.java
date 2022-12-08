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

import java.io.IOException;

public class Studio extends Application {

  public static Stage stage;

  public static VPinStudioClient client;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;

    //replace the OverlayFX client with the Studio one
    Studio.client = new VPinStudioClient("localhost");
    OverlayWindowFX.client = Studio.client;

    int width = 1920;
    int height = 1080;
    Parent root = null;
    boolean resizeable = true;
    String title = "VPin Studio";
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();

    boolean localPingResult = false; //client.ping();
    if (localPingResult) {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("scene-root.fxml"));
      root = loader.load();

      if (screenBounds.getHeight() >= 1280) {
        height = 1200;
      }
      if (screenBounds.getHeight() >= 1480) {
        height = 1400;
      }
    }
    else {
      FXMLLoader loader = new FXMLLoader(LauncherController.class.getResource("scene-launcher.fxml"));
      root = loader.load();
      title = "VPin Studio Launcher";
      width = 800;
      height = 400;
      resizeable = false;
    }


    Scene scene = new Scene(root, width, height);
    scene.setFill(Paint.valueOf("#212529"));
    stage.setTitle(title);
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
    stage.setScene(scene);
    stage.setResizable(resizeable);

    if(resizeable) {
      stage.initStyle(StageStyle.UNDECORATED);
    }

    stage.setX((screenBounds.getWidth() / 2) - (width / 2));
    stage.setY((screenBounds.getHeight() / 2) - (height / 2));

    if(resizeable) {
      ResizeHelper.addResizeListener(stage, width, 1080, width * 2, height * 2);
    }

    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}