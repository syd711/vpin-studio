package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationStage {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationStage.class);

  private final static int WIDTH = 2400;
  public static final int OFFSET = 50;
  private static double scaling = 0.5;

  private final Notification notification;
  private final NotificationSettings notificationSettings;
  private Transition inTransition;
  private Transition outTransition;
  private Stage stage;

  public NotificationStage(Notification notification, NotificationSettings notificationSettings) {
    this.notification = notification;
    this.notificationSettings = notificationSettings;
    stage = new Stage();
    stage.setTitle("Pause Menu");
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setAlwaysOnTop(true);
    Scene scene = null;

    inTransition = null;
    try {
      stage.getIcons().add(new Image(NotificationController.class.getResourceAsStream("logo-64.png")));

      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(NotificationController.class.getResource("notification.fxml"));
      BorderPane root = loader.load();
      NotificationController controller = loader.getController();
      controller.setNotification(notification);
      scaleStage(root, screenBounds);

      if (screenBounds.getWidth() > screenBounds.getHeight()) {
        LOG.info("Window Mode: Landscape");
        root.setRotate(-90);
        root.setTranslateY((screenBounds.getHeight()) + OFFSET);
        root.setTranslateX(-(WIDTH * scaling / 2) + OFFSET);
        inTransition = TransitionUtil.createTranslateByYTransition(root, 300, (int) -(WIDTH * scaling));
        outTransition = TransitionUtil.createTranslateByYTransition(root, 300, (int) (WIDTH * scaling));

        stage.setY(0);
        stage.setX(0);
        scene = new Scene(root, WIDTH * scaling, screenBounds.getHeight());
      }
      else {
        LOG.info("Window Mode: Portrait");
        root.setTranslateY(-(WIDTH * scaling / 2) - OFFSET);
        root.setTranslateX(-(screenBounds.getWidth() / 2 - ((WIDTH * scaling) / 2) + (WIDTH * scaling)));
        inTransition = TransitionUtil.createTranslateByXTransition(root, 300, (int) (WIDTH * scaling));
        outTransition = TransitionUtil.createTranslateByXTransition(root, 300, (int) -(WIDTH * scaling));

        stage.setY(0);
        stage.setX(0);
        scene = new Scene(root, screenBounds.getWidth(), WIDTH * scaling);
      }

      scene.setFill(Color.TRANSPARENT);
      stage.setScene(scene);
    } catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }


  public void show() {
    startTransition();
    stage.show();
  }

  private void startTransition() {
    Platform.runLater(() -> {
      inTransition.onFinishedProperty().set(event -> {
        try {
          Thread.sleep(notificationSettings.getDurationSec() * 1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        outTransition.onFinishedProperty().set(event1 -> stage.close());
        outTransition.play();
      });
      inTransition.play();
    });
  }

  private static void scaleStage(BorderPane root, Rectangle2D screenBounds) {
    double max = Math.max(screenBounds.getWidth(), screenBounds.getHeight());
    if (max > 2560) {
      scaling = 0.5;
    }
    else if (max > 1920) {
      scaling = 0.4;
    }
    root.setScaleX(scaling);
    root.setScaleY(scaling);
  }

  public Stage getStage() {
    return stage;
  }
}
