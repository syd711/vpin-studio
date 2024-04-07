package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.PopperScreenController;
import de.mephisto.vpin.commons.fx.pausemenu.model.PopperScreenAsset;
import de.mephisto.vpin.commons.utils.TransitionUtil;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PopperScreensManager {
  private final static Logger LOG = LoggerFactory.getLogger(PopperScreensManager.class);

  private static PopperScreensManager instance = new PopperScreensManager();

  public static PopperScreensManager getInstance() {
    return instance;
  }

  public Stage showScreen(@NonNull PopperScreenAsset asset) {
    try {
      BorderPane root = new BorderPane();
      root.setStyle("-fx-background-color: transparent;");
      Screen screen = Screen.getPrimary();
      final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);
      scene.setFill(Color.TRANSPARENT);
      scene.setCursor(Cursor.NONE);

      Stage screenStage = new Stage();
      screenStage.setTitle("Asset: " + asset.getName());
      screenStage.setScene(scene);
      screenStage.initStyle(StageStyle.TRANSPARENT);
      screenStage.setAlwaysOnTop(true);

      asset.setScreenStage(screenStage);

      try {
        String resource = "scene-popper-screen.fxml";
        FXMLLoader loader = new FXMLLoader(PopperScreenController.class.getResource(resource));
        Parent widgetRoot = loader.load();
        PopperScreenController screenController = loader.getController();
        screenController.setMediaAsset(asset);
        root.setCenter(widgetRoot);
      } catch (IOException e) {
        LOG.error("Failed to Popper screen: " + e.getMessage(), e);
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
          OverlayWindowFX.toFront(stage, stage.isShowing());
        }
      } catch (InterruptedException e) {
        //ignore
      }
    }).start();
  }
}
