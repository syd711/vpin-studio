package de.mephisto.vpin.commons.fx.apng;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ApngImageViewDemo extends Application {

  @Override
  public void start(final Stage stage) {
  
    ApngImageLoaderFactory.install();

    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout, 500, 500));

    Pane pane = new Pane();
    pane.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));

    File wheelIcon;
    //wheelIcon = new File("./testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws.png");
    //wheelIcon = new File("./testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).gif");
    //wheelIcon = new File("./testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).apng");
    wheelIcon = new File("./testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Atlantis (Bally 1989).apng");

    try (InputStream imgStream = new FileInputStream(wheelIcon)) {
      Image image = new Image(imgStream);
      if (image.isError()) {
        System.err.println("error creating image");
        image.getException().printStackTrace();
      }

      ImageView view = new ImageView();
      view.setImage(image);

      pane.getChildren().add(view);
      layout.setCenter(pane);

      stage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
