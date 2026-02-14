package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.util.SystemUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationStageService extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static NotificationStageService INSTANCE = new NotificationStageService();

  private final static int MAX_NOTIFICATIONS = 3;

  private final static Queue<Notification> queue = new ConcurrentLinkedQueue<>();
  private final static Queue<NotificationStage> stages = new ConcurrentLinkedQueue<>();
  private final static AtomicBoolean locked = new AtomicBoolean(false);

  public static NotificationStageService getInstance() {
    return INSTANCE;
  }

  public static void main(String[] args) {
    launch(args);
  }

  public void queueNotification(Notification notification, boolean poll) {
    queue.offer(notification);
    if (poll) {
      pollNotifications();
    }
    else {
      LOG.info("Queued " + notification);
      SLOG.info("Queued " + notification);
    }
  }

  public void pollNotifications() {
    LOG.info("Polling notifications (Queue size: " + queue.size() + ")");
    Platform.runLater(() -> {
      pollQueue();
    });
  }

  public void setMaxNotificationsLock(boolean b) {
    locked.set(b);
    if (!locked.get()) {
      pollQueue();
    }
  }

  private synchronized void pollQueue() {
    if (locked.get()) {
      return;
    }

    if (!queue.isEmpty() && stages.size() < MAX_NOTIFICATIONS) {
      for (NotificationStage stage : stages) {
        stage.move();
      }
      setMaxNotificationsLock(true);
      showNotificationStage();
    }
  }

  private void showNotificationStage() {
    Notification notification = queue.poll();
    NotificationStage notificationStage = new NotificationStage(notification);
    stages.offer(notificationStage);
    LOG.info("Showing " + notification);
    SLOG.info("Showing " + notification);
    notificationStage.getStage().setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        setMaxNotificationsLock(false);
        stages.poll();
        pollQueue();
      }
    });
    notificationStage.show();
  }

  @Override
  public void start(Stage stage) {
    Notification notification1 = new Notification();
    notification1.setTitle1("This is a super long name with maybe too much text");
    notification1.setTitle2("This is a super long name with maybe too much text");
    notification1.setTitle3("This is a super long name with maybe too much text");
    notification1.setDurationSec(3);
    notification1.setDesktopMode(true);

    Notification notification2 = new Notification();
    notification2.setTitle1("Test2");
    notification2.setDurationSec(3);
    notification2.setDesktopMode(true);

    Notification notification3 = new Notification();
    notification3.setTitle1("Test3");
    notification3.setDurationSec(3);
    notification3.setDesktopMode(true);

    Notification notification4 = new Notification();
    notification4.setTitle1("Test3");
    notification4.setDurationSec(3);
    notification4.setDesktopMode(true);

    queueNotification(notification1, true);
    queueNotification(notification2, true);
    queueNotification(notification3, true);

//    pollNotifications();
//    showNotification(notification4);
  }
}
