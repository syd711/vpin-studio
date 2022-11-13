package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.VPinStudioClient;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Studio extends Application {

  private double xOffset;
  private double yOffset;

  public static Stage stage;

  public static VPinStudioClient client;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;
    Studio.client = VPinStudioClient.create();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("scene-root.fxml"));
    Parent root = loader.load();
    NavigationController.navigationController = loader.<StudioFXController>getController();

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

    scene.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
      }
    });
    scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
      }
    });
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}