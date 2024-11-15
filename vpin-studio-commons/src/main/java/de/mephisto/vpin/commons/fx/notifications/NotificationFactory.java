package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.connectors.mania.util.ImageUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class NotificationFactory {

  // copied from Game.getWheelImage()
  private static Image getImage(File file) {
    Image image = null;
    if (file != null && file.exists()) {
      try {
        BufferedImage bufferedImage = ImageUtil.loadImage(file);
        image = SwingFXUtils.toFXImage(bufferedImage, null);
      }
      catch (IOException e) {
        //throw new RuntimeException(e);
      }
    }
    return image;
  }

  public static Notification createNotification(File wheeFile, String gameDisplayName, String title) {
    Notification notification = new Notification();
    notification.setImage(getImage(wheeFile));
    notification.setTitle1(gameDisplayName);
    notification.setTitle2(title);
    return notification;
  }

  public static Notification createNotification(File wheeFile, String gameDisplayName, String title, String subtitle) {
    Notification notification = new Notification();
    notification.setImage(getImage(wheeFile));
    notification.setTitle1(gameDisplayName);
    notification.setTitle2(title);
    notification.setTitle3(subtitle);
    return notification;
  }
}
