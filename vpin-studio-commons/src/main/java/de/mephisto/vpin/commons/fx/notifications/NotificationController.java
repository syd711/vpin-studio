package de.mephisto.vpin.commons.fx.notifications;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class NotificationController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Label title1;

  @FXML
  private Label title2;

  @FXML
  private Label title3;

  @FXML
  private ImageView logoImageView;

  @FXML
  private ImageView rowImageView;

  @FXML
  private ImageView rowIconImageView;

  @FXML
  private VBox labelContainer;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      File themeFolder = new File("./resources/themes/notifications");
      if (themeFolder.exists()) {
        File row = new File(themeFolder, "row.png");
        if (row.exists()) {
          Image image = new Image(new FileInputStream(row));
          rowImageView.setImage(image);
        }

        File rowImage = new File(themeFolder, "logo.png");
        if (rowImage.exists()) {
          Image image = new Image(new FileInputStream(rowImage));
          rowIconImageView.setImage(image);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to theme notifications: " + e.getMessage(), e);
    }
  }

  public void setNotification(Notification notification) {
    if (notification.getImage() != null) {
      logoImageView.setImage(notification.getImage());
    }
    else {
      logoImageView.setVisible(false);
    }

    if (notification.getTitle1() != null) {
      title1.setText(notification.getTitle1());
    }
    else {
      title1.setText("");
    }

    if (notification.getTitle2() != null) {
      title2.setText(notification.getTitle2());
    }
    else {
      title2.setText("");
    }

    if (!StringUtils.isEmpty(notification.getTitle3())) {
      title3.setText(notification.getTitle3());
    }
    else {
      title3.setText("");
    }

    NotificationSettings notificationSettings = ServerFX.client.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
    MonitorInfo screen = ServerFX.client.getSystemService().getScreenInfo(notificationSettings.getNotificationsScreenId());
    int padding = 0;
    padding += notification.getTextBoxMargin();

    if (screen.getHeight() > 2000) {
      labelContainer.setPadding(new Insets(0, 0, 0, 650 + padding));
    }
    if (title1.getMaxWidth() > 0 && notification.getTextBoxMargin() > 0) {
      title1.setMaxWidth(title1.getMaxWidth() - notification.getTextBoxMargin());
      title2.setMaxWidth(title1.getMaxWidth());
      title3.setMaxWidth(title1.getMaxWidth());
    }
  }
}
