package de.mephisto.vpin.commons.fx.notifications;

import javafx.scene.image.Image;

public class NotificationFactory {

  public static Notification createNotification(Image image, String gameDisplayName, String title) {
    Notification notification = new Notification();
    notification.setImage(image);
    notification.setTitle1(gameDisplayName);
    notification.setTitle2(title);
    return notification;
  }

  public static Notification createNotification(Image image, String gameDisplayName, String title, String subtitle) {
    Notification notification = new Notification();
    notification.setImage(image);
    notification.setTitle1(gameDisplayName);
    notification.setTitle2(title);
    notification.setTitle3(subtitle);
    return notification;
  }
}
