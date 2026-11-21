package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.FrontendScreenController;
import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import org.jspecify.annotations.NonNull;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class FrontendScreensManager {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final FrontendScreensManager instance = new FrontendScreensManager();

  public static FrontendScreensManager getInstance() {
    return instance;
  }

  public Stage showScreen(@NonNull FrontendScreenAsset asset) {
    try {
      BorderPane root = new BorderPane();
      root.setStyle("-fx-background-color: transparent;");

      MonitorInfo screen = ServerFX.client.getSystemService().getScreenInfo(-1);
      //plain 2D scene: these stages are shown on top of a running emulator, so no depth buffer and no MSAA surfaces
      final Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
      scene.setFill(Color.TRANSPARENT);
      scene.setCursor(Cursor.NONE);

      Stage screenStage = new Stage();
      screenStage.setTitle("Asset: " + asset.getName());
      screenStage.setScene(scene);
      screenStage.initStyle(StageStyle.TRANSPARENT);
      screenStage.setAlwaysOnTop(true);

      asset.setScreenStage(screenStage);

      try {
        String resource = "scene-frontend-screen.fxml";
        FXMLLoader loader = new FXMLLoader(FrontendScreenController.class.getResource(resource));
        loader.setResources(Messages.getBundle());
        Parent widgetRoot = loader.load();
        FrontendScreenController screenController = loader.getController();
        screenController.setMediaAsset(asset);
        root.setCenter(widgetRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to frontend screen: " + e.getMessage(), e);
      }

      showStage(screenStage, asset);
      return screenStage;
    }
    catch (Exception e) {
      LOG.error("Failed to open screen stage: " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * Shows the stage. Stages with a duration are closed and disposed afterwards, stages without a duration
   * stay open until the caller disposes them (see PauseMenu#exitPauseMenu).
   */
  private void showStage(@NonNull Stage stage, @NonNull FrontendScreenAsset asset) {
    int duration = asset.getDuration();
    stage.show();
    TransitionUtil.createInFader(stage.getScene().getRoot(), 500).play();
    new Thread(() -> {
      try {
        stage.getScene().setCursor(Cursor.NONE);
        if (duration > 0) {
          Thread.sleep(duration * 1000L);
          Platform.runLater(() -> {
            FadeTransition outFader = TransitionUtil.createOutFader(stage.getScene().getRoot(), 500);
            outFader.setOnFinished(event -> closeStage(stage, asset));
            outFader.play();
          });
        }
        else {
          ServerFX.toFront(stage, stage.isShowing());
        }
      }
      catch (InterruptedException e) {
        //ignore
      }
    }, "Frontend Screen Timer").start();
  }

  /**
   * Hiding the stage alone keeps the media alive: a looping JavaFX media player would keep decoding (with audio)
   * and a launched VLC process would keep playing on top of the emulator. Must be called on the FX thread.
   */
  private void closeStage(@NonNull Stage stage, @NonNull FrontendScreenAsset asset) {
    stage.hide();
    try {
      FrontendScreenController controller = asset.getFrontendScreenController();
      if (controller != null) {
        controller.dispose();
      }
      LOG.info("Closed and disposed screen asset \"{}\"", asset.getName());
    }
    catch (Exception e) {
      LOG.error("Failed to dispose screen asset \"{}\": {}", asset.getName(), e.getMessage(), e);
    }
  }
}
