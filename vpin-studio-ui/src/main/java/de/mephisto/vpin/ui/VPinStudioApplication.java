package de.mephisto.vpin.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class VPinStudioApplication extends Application {

  private double xOffset;
  private double yOffset;

  public static Stage stage;

  @Override
  public void start(Stage stage) throws IOException {
    VPinStudioApplication.stage = stage;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("scene-root.fxml"));
    Parent root = loader.load();
    NavigationController.navigationController = loader.<StudioFXController>getController();

    Scene scene = new Scene(root, 1920, 1080);
    scene.setFill(Paint.valueOf("#212529"));
    stage.setTitle("VPin Studio");
    stage.setScene(scene);
    stage.initStyle(StageStyle.UNDECORATED);

    stage.setX(400);
    stage.setY(200);

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