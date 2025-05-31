package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
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
  private Rectangle2D screenBounds;
  private NotificationController notificationController;

  private BorderPane root;
  private Transition inTransition;
  private Transition outTransition;
  private Stage stage;

  public NotificationStage(Notification notification) {
    this.notification = notification;
    stage = new Stage();
    stage.setTitle(notification.getWindowTitle());
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setAlwaysOnTop(true);
    Scene scene = null;

    inTransition = null;
    try {
      stage.getIcons().add(new Image(NotificationController.class.getResourceAsStream("logo-64.png")));

      NotificationSettings notificationSettings = ServerFX.client.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
      Screen screen = SystemUtil.getScreenById(notificationSettings.getNotificationsScreenId());
      screenBounds = screen.getBounds();

      FXMLLoader loader = new FXMLLoader(NotificationController.class.getResource("notification.fxml"));
      root = loader.load();
      notificationController = loader.getController();
      notificationController.setNotification(notification);
      scaleStage(root, screenBounds);

      if (screenBounds.getWidth() > screenBounds.getHeight() && !notification.isDesktopMode()) {
        LOG.info("Window Mode: Portrait");
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
        stage.setX(screenBounds.getMinX());
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight() / 2);
      }
      else {
        LOG.info("Window Mode: Landscape");
        double y = 0;
        double width = root.getPrefWidth() * scaling * scaling;
        double x = screenBounds.getWidth() - width;

        if (screenBounds.getWidth() < 2000) {
          y = -380;
          x -= 90;
        }
        else if (screenBounds.getWidth() < 2700) {
          y = -460;
          x += -140;
        }
        else if (screenBounds.getWidth() < 4000) {
          y = -560;
          x += -50;
        }
        root.setTranslateY(y);
        root.setTranslateX(-x);
        inTransition = TransitionUtil.createTranslateByXTransition(root, 300, (int) (screenBounds.getWidth() / 2));
        outTransition = TransitionUtil.createTranslateByXTransition(root, 300, (int) -(screenBounds.getWidth() / 2));

        stage.setY(0);
        stage.setX(screenBounds.getMinX());
        scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight() / 2);
      }

      scene.setFill(Color.TRANSPARENT);
      stage.setScene(scene);
    }
    catch (Exception e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }


  public void move() {
    Transition transition = null;
    if (screenBounds.getWidth() > screenBounds.getHeight() && !notification.isDesktopMode()) {
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
          }
          catch (InterruptedException e) {
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
