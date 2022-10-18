package de.mephisto.vpin.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
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
    FXMLLoader fxmlLoader = new FXMLLoader(VPinStudioApplication.class.getResource("scene-tables.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
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