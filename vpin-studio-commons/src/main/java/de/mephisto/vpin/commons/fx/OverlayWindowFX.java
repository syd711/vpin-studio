package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.restclient.VPinStudioClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class OverlayWindowFX extends Application {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(OverlayWindowFX.class);

  public static final CountDownLatch latch = new CountDownLatch(1);
  public static OverlayWindowFX overlayFX = null;

  private Stage stage;

  public static VPinStudioClient client;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public static OverlayWindowFX waitForOverlay() {
    try {
      latch.await();
      LOG.info("OverlayFX creation finished.");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return overlayFX;
  }

  public void setVisible(boolean b) {
    if(b) {
      stage.show();
    }
    else {
      stage.hide();
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    OverlayWindowFX.client = VPinStudioClient.create();

    this.stage = primaryStage;
    Platform.setImplicitExit(false);

    Group root = new Group();
    Screen screen = Screen.getPrimary();
    final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);

    Rectangle2D bounds = screen.getVisualBounds();
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());

    stage.setScene(scene);
    stage.setFullScreenExitHint("");
    stage.setFullScreen(true);
    stage.setAlwaysOnTop(true);
    stage.setHeight(screen.getVisualBounds().getWidth());
    stage.setWidth(screen.getVisualBounds().getHeight());

    overlayFX = this;
    latch.countDown();
  }
}
