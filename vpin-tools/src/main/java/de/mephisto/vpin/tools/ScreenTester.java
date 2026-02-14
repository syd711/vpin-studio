package de.mephisto.vpin.tools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ScreenTester extends Application {
  @Override
  public void start(Stage stage) {
    StackPane root = new StackPane(new Label("Rotated Root"));
    root.setPrefSize(400, 300);
    Scene scene = new Scene(root);

    root.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
      @Override
      public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        Platform.runLater(() -> {
          Screen screen = Screen.getScreens().get(1);
          Rectangle2D vb = screen.getVisualBounds();
          double scale = screen.getOutputScaleX();

          double stageW = stage.getWidth();
          double stageH = stage.getHeight();

          double logicalX = vb.getMinX() + (vb.getWidth() - stageW) / 2;
          double logicalY = vb.getMinY() + (vb.getHeight() - stageH) / 2;

// Set logical coords
          stage.setX(-3840);
          stage.setY(screen.getBounds().getMinY());

// Physical verification
          System.out.println("Physical X = " + logicalX * scale);
          System.out.println("Physical Y = " + logicalY * scale);
        });
      }
    });


    stage.setScene(scene);
    stage.show();
  }
}
