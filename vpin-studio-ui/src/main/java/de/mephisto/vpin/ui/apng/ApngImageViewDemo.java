package de.mephisto.vpin.ui.apng;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
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
    pane.setBackground(Background.fill(Color.PINK));

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
      ImageView iv = new ImageView(image);
      pane.getChildren().add(iv);
    } 
    catch (Exception e) {
      System.err.println("error loading image");
      e.printStackTrace();
    }
    
    layout.setCenter(pane);
    stage.show();
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
