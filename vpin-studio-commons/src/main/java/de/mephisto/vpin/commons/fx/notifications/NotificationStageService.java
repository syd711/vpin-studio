package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import javafx.application.Application;
import javafx.stage.Stage;

public class NotificationStageService extends Application {
  private static NotificationStageService INSTANCE = new NotificationStageService();

  public static NotificationStageService getInstance() {
    return INSTANCE;
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    Notification notification = new Notification();
    new NotificationStage(notification, new NotificationSettings()).show();
  }

  public NotificationStage showNotification(Notification notification, NotificationSettings notificationSettings) {
    return new NotificationStage(notification, notificationSettings);
  }
}
