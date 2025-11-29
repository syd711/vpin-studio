package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.FrontendScreenController;
import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class FrontendScreensManager {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static FrontendScreensManager instance = new FrontendScreensManager();

  public static FrontendScreensManager getInstance() {
    return instance;
  }

  public Stage showScreen(@NonNull FrontendScreenAsset asset) {
    try {
      BorderPane root = new BorderPane();
      root.setStyle("-fx-background-color: transparent;");

      MonitorInfo screen = ServerFX.client.getSystemService().getScreenInfo(-1);
      final Scene scene = new Scene(root, screen.getWidth(), screen.getHeight(), true, SceneAntialiasing.BALANCED);
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
        Parent widgetRoot = loader.load();
        FrontendScreenController screenController = loader.getController();
        screenController.setMediaAsset(asset);
        root.setCenter(widgetRoot);
      } catch (IOException e) {
        LOG.error("Failed to frontend screen: " + e.getMessage(), e);
      }

      showStage(screenStage, asset.getDuration());
      return screenStage;
    } catch (Exception e) {
      LOG.error("Failed to open screen stage: " + e.getMessage(), e);
    }
    return null;
  }

  private void showStage(Stage stage, int duration) {
    stage.show();
    TransitionUtil.createInFader(stage.getScene().getRoot(), 500).play();
    new Thread(() -> {
      try {
        stage.getScene().setCursor(Cursor.NONE);
        if (duration > 0) {
          Thread.sleep(duration * 1000);
          Platform.runLater(() -> {
            FadeTransition outFader = TransitionUtil.createOutFader(stage.getScene().getRoot(), 500);
            outFader.setOnFinished(event -> stage.hide());
            outFader.play();
          });
        }
        else {
          ServerFX.toFront(stage, stage.isShowing());
        }
      } catch (InterruptedException e) {
        //ignore
      }
    }).start();
  }
}
