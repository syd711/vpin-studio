package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.VPinStudioClient;
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
    Studio.client = VPinStudioClient.create();
    OverlayWindowFX.client = Studio.client;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("scene-root.fxml"));
    Parent root = loader.load();
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    int height = 1080;
    int width = 1920;
    if(screenBounds.getHeight() >= 1280) {
      height = 1200;
    }
    if(screenBounds.getHeight() >= 1480) {
      height = 1400;
    }

    Scene scene = new Scene(root, width, height);
    scene.setFill(Paint.valueOf("#212529"));
    stage.setTitle("VPin Studio");
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
    stage.setScene(scene);
    stage.initStyle(StageStyle.UNDECORATED);

    stage.setX((screenBounds.getWidth()/2) - (width/2));
    stage.setY((screenBounds.getHeight()/2) - (height/2));

    ResizeHelper.addResizeListener(stage);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}