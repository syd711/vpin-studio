package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationStage;
import de.mephisto.vpin.commons.fx.notifications.NotificationStageService;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class NotificationService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private PreferencesService preferencesService;

  private final static int MAX_NOTIFICATIONS = 3;

  private final Queue<Notification> queue = new ConcurrentLinkedQueue();
  private final Queue<Notification> stages = new ConcurrentLinkedQueue<>();

  private NotificationSettings notificationSettings;

  public void showNotification(Notification notification) {
    queue.offer(notification);
    LOG.info("Queue size: " + queue.size());
    Platform.runLater(() -> {
      pollQueue();
    });
  }

  private synchronized void pollQueue() {
    if (!queue.isEmpty()) {
      Notification notification = queue.poll();
      NotificationStage notificationStage = new NotificationStage(notification, notificationSettings);
      notificationStage.getStage().setOnHiding(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          System.out.println("closed");
        }
      });
      notificationStage.show();
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.NOTIFICATION_SETTINGS)) {
      notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);

    Notification startup = new Notification();
    startup.setImage(new Image(NotificationStage.class.getResourceAsStream("logo.png")));
    startup.setTitle1("VPin Studio Server");
    startup.setTitle2("The server has been started.");
    showNotification(startup);
  }
}
