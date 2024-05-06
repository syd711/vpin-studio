package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationStage;
import de.mephisto.vpin.commons.fx.notifications.NotificationStageService;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private PreferencesService preferencesService;

  private NotificationSettings notificationSettings;

  public void showNotification(Notification notification) {
    notification.setDurationSec(notification.getDurationSec());
    NotificationStageService.getInstance().showNotification(notification);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.NOTIFICATION_SETTINGS)) {
      notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
      LOG.info("Notification settings have been updated.");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);

    Notification startup = new Notification();
    startup.setImage(new Image(NotificationStage.class.getResourceAsStream("logo.png")));
    startup.setTitle1("VPin Studio Server");
    startup.setTitle2("The server has been started.");
    startup.setDurationSec(notificationSettings.getDurationSec());
    showNotification(startup);
  }
}
