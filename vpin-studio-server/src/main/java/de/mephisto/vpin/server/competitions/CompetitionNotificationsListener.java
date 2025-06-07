package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompetitionNotificationsListener implements CompetitionChangeListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionNotificationsListener.class);

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameService gameService;

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    if (isEnabled()) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        File wheelfile = gameService.getWheelImage(game);
        Notification notification = NotificationFactory.createNotification(wheelfile, game.getGameDisplayName(), "Competition \"" + competition.getName() + "\" started.");
        notificationService.showNotification(notification);
      }
    }
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {

  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {

  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    if (isEnabled()) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        File wheelfile = gameService.getWheelImage(game);
        Notification notification = NotificationFactory.createNotification(wheelfile, game.getGameDisplayName(), "Competition \"" + competition.getName() + "\" finished.");
        notificationService.showNotification(notification);
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
  }

  private boolean isEnabled() {
    NotificationSettings notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
    return notificationSettings.isCompetitionNotification();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionLifecycleService.addCompetitionChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
