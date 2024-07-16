package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.ServerFXListener;
import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationFactory;
import de.mephisto.vpin.commons.fx.notifications.NotificationStageService;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements InitializingBean, PreferenceChangedListener, HighscoreChangeListener, FrontendStatusChangeListener, ServerFXListener {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private FrontendService frontendService;

  private NotificationSettings notificationSettings;

  public void showNotification(Notification notification) {
    //no support for standalone
    if (frontendService.getFrontendType().isNotStandalone()) {
      if (Features.NOTIFICATIONS_ENABLED && notificationSettings.getDurationSec() > 0) {
        notification.setDurationSec(notificationSettings.getDurationSec());
        NotificationStageService.getInstance().queueNotification(notification);
      }
    }
    else {
      LOG.info("Notifications not supported in standalone mode.");
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
    if (notificationSettings.getDurationSec() == 0) {
      LOG.info("Skipped notification, duration is not set.");
      return;
    }

    //e.g. we want to ignore notifications on manual (bulk) scans
    EventOrigin origin = event.getEventOrigin();
    if (!origin.equals(EventOrigin.TABLE_EXIT_EVENT) &&
        !origin.equals(EventOrigin.TABLE_LAUNCH_EVENT) &&
        !origin.equals(EventOrigin.FRONTEND_LAUNCH_EVENT)) {
      return;
    }

    Game game = event.getGame();
    if (notificationSettings.isHighscoreUpdatedNotification()) {
      Notification notification = NotificationFactory.createNotification(game.getWheelImage(),
          game.getGameDisplayName(), "A new highscore has been created!",
          event.getNewScore().getPosition() + ". " + event.getNewScore().getPlayerInitials() + "\t" + event.getNewScore().getFormattedScore());
      showNotification(notification);
    }

    if (notificationSettings.isDiscordNotification()) {
      String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
      String defaultChannelId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID);
      if (!StringUtils.isEmpty(guildId) && !StringUtils.isEmpty(defaultChannelId)) {
        Notification notification = NotificationFactory.createNotification(game.getWheelImage(), game.getGameDisplayName(), "Scores has been published on Discord!");
        showNotification(notification);
      }
    }
  }

  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  @Override
  public void frontendLaunched() {

  }

  @Override
  public void frontendExited() {
    NotificationStageService.getInstance().pollNotifications();
  }

  @Override
  public void frontendRestarted() {
    NotificationStageService.getInstance().pollNotifications();
  }

  @Override
  public void fxInitialized() {
    if (notificationSettings.isStartupNotification() && notificationSettings.getDurationSec() > 0) {
      Notification notification = NotificationFactory.createNotification(null,
          "VPin Studio Server", "The server has been started.",
          "Version " + systemService.getVersion());
      showNotification(notification);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      if (Features.NOTIFICATIONS_ENABLED) {
        ServerFX.addListener(this);

        preferencesService.addChangeListener(this);
        highscoreService.addHighscoreChangeListener(this);
        frontendStatusService.addFrontendStatusChangeListener(this);
        preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to initialize " + this + ": " + e.getMessage(), e);
    }
  }
}
