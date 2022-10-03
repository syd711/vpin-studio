package de.mephisto.vpin.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VPinStudioApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(VPinStudioApplication.class.getResource("main.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 1920, 1280);
    stage.setTitle("VPin Studio");
    stage.setScene(scene);

    stage.setX(400);
    stage.setY(200);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}