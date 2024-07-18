package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.commons.utils.TransitionUtil;
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
import org.apache.commons.lang3.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class NotificationStage {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationStage.class);

  private final static int WIDTH = 2400;
  public static final int OFFSET = 30;
  private static double scaling = 0.5;
  private final Notification notification;
  private NotificationController notificationController;

  private BorderPane root;
  private Transition inTransition;
  private Transition outTransition;
  private Stage stage;

  public NotificationStage(Notification notification) {
    this.notification = notification;
    stage = new Stage();
    stage.setTitle("VPin UI");
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setAlwaysOnTop(true);
    Scene scene = null;

    inTransition = null;
    try {
      stage.getIcons().add(new Image(NotificationController.class.getResourceAsStream("logo-64.png")));

      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(NotificationController.class.getResource("notification.fxml"));
      root = loader.load();
      notificationController = loader.getController();
      notificationController.setNotification(notification);
      scaleStage(root, screenBounds);

      if (screenBounds.getWidth() > screenBounds.getHeight()) {
        LOG.info("Window Mode: Landscape");
        root.setRotate(-90);
        //WQHD
        double y = (screenBounds.getHeight() / 2);
        //HD
        if (screenBounds.getHeight() < 1100) {
          y = 600;
        }
        else if (screenBounds.getHeight() > 2000) {
          y = 1080;
        }

        root.setTranslateY(y);
        root.setTranslateX(-(screenBounds.getWidth() / 2));
        inTransition = TransitionUtil.createTranslateByYTransition(root, 300, (int) -(screenBounds.getHeight() / 2));
        outTransition = TransitionUtil.createTranslateByYTransition(root, 300, (int) (screenBounds.getHeight() / 2));

        stage.setY(screenBounds.getHeight() / 2);
        stage.setX(0);
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight() / 2);
      }
      else {
        LOG.info("Window Mode: Portrait");
        root.setTranslateY(-500);
        double width = root.getPrefWidth() * scaling * scaling;
        double x = screenBounds.getWidth() - width;
        root.setTranslateX(-x);
        inTransition = TransitionUtil.createTranslateByXTransition(root, 300, (int) (screenBounds.getWidth() / 2));
        outTransition = TransitionUtil.createTranslateByXTransition(root, 300, (int) -(screenBounds.getWidth() / 2));

        stage.setY(0);
        stage.setX(0);
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight() / 2);
      }

      scene.setFill(Color.TRANSPARENT);
      stage.setScene(scene);
    } catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }


  public void move() {
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    Transition transition = null;
    if (screenBounds.getWidth() > screenBounds.getHeight()) {
      transition = TransitionUtil.createTranslateByXTransition(root, 300, (int) (WIDTH * scaling / 3) + OFFSET);
    }
    else {
      transition = TransitionUtil.createTranslateByYTransition(root, 300, (int) (WIDTH * scaling / 3) + OFFSET);
    }
    transition.play();
  }

  public void show() {
    startTransitions();
    stage.show();
  }

  private void startTransitions() {
    Platform.runLater(() -> {
      outTransition.onFinishedProperty().set(event1 -> {
        stage.close();
      });

      inTransition.onFinishedProperty().set(event -> {
        NotificationStageService.getInstance().setMaxNotificationsLock(false);
        new Thread(() -> {
          try {
            ThreadUtils.sleep(Duration.of(notification.getDurationSec(), ChronoUnit.SECONDS));
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          Platform.runLater(() -> {
            outTransition.play();
          });
        }).start();
      });
      inTransition.play();
    });
  }

  private static void scaleStage(BorderPane root, Rectangle2D screenBounds) {
    if (screenBounds.getWidth() > screenBounds.getHeight()) {
      double targetSize = screenBounds.getHeight() / 2;
      scaling = targetSize / WIDTH;
    }
    else {
      double targetSize = screenBounds.getWidth() / 2;
      scaling = targetSize / WIDTH;
    }

    root.setScaleX(scaling);
    root.setScaleY(scaling);
  }

  public Stage getStage() {
    return stage;
  }

  @Override
  public String toString() {
    return "Notification Stage [" + notification + "]";
  }
}
