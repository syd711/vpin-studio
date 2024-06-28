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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service //TODO not a service
public class CompetitionNotificationsListener implements CompetitionChangeListener, InitializingBean {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameService gameService;

  @Override
  public void competitionStarted(@NotNull Competition competition) {
    if (isEnabled()) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        Notification notification = NotificationFactory.createNotification(game.getWheelImage(), game.getGameDisplayName(), "Competition \"" + competition.getName() + "\" started.");
        notificationService.showNotification(notification);
      }
    }
  }

  @Override
  public void competitionCreated(@NotNull Competition competition) {

  }

  @Override
  public void competitionChanged(@NotNull Competition competition) {

  }

  @Override
  public void competitionFinished(@NotNull Competition competition, @Nullable Player winner, @NotNull ScoreSummary scoreSummary) {
    if (isEnabled()) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        Notification notification = NotificationFactory.createNotification(game.getWheelImage(), game.getGameDisplayName(), "Competition \"" + competition.getName() + "\" finished.");
        notificationService.showNotification(notification);
      }
    }
  }

  @Override
  public void competitionDeleted(@NotNull Competition competition) {
  }

  private boolean isEnabled() {
    NotificationSettings notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
    return notificationSettings.isCompetitionNotification();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionService.addCompetitionChangeListener(this);
  }
}
