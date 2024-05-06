package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.ServerFXListener;
import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationStageService;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements InitializingBean, PreferenceChangedListener, HighscoreChangeListener, ServerFXListener {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private SystemService systemService;

  private NotificationSettings notificationSettings;

  public void showNotification(Notification notification) {
    if (Features.NOTIFICATIONS_ENABLED) {
      notification.setDurationSec(notificationSettings.getDurationSec());
      NotificationStageService.getInstance().showNotification(notification);
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.NOTIFICATION_SETTINGS)) {
      notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
    }
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    if (!notificationSettings.isHighscoreUpdatedNotification() || notificationSettings.getDurationSec() == 0) {
      return;
    }

    Game game = event.getGame();
    Notification notification = new Notification();
    notification.setImage(game.getWheelImage());
    notification.setTitle1(game.getGameDisplayName());
    notification.setTitle2("A new highscore has been created!");
    notification.setTitle3(event.getNewScore().getPosition() + ". " + event.getNewScore().getPlayerInitials() + "\t" + event.getNewScore().getScore());
    showNotification(notification);
  }

  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      if (Features.NOTIFICATIONS_ENABLED) {
        ServerFX.addListener(this);

        preferencesService.addChangeListener(this);
        highscoreService.addHighscoreChangeListener(this);
        preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);
      }
    } catch (Exception e) {
      LOG.error("Failed to initialize " + this + ": " + e.getMessage(), e);
    }
  }

  @Override
  public void toolkitRead() {
    if (notificationSettings.isStartupNotification() && notificationSettings.getDurationSec() > 0) {
      Notification startup = new Notification();
      startup.setImage(null);
      startup.setTitle1("VPin Studio Server");
      startup.setTitle2("The server has been started.");
      startup.setTitle3("Version " + systemService.getVersion());
      showNotification(startup);
    }
  }
}
