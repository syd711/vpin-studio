package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.IScoredResult;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class IScoredService implements PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredService.class);

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  private NotificationSettings notificationSettings;

  public boolean deleteGameRoom(String gameRoomId) throws Exception {
    IScoredSettings settings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    Optional<IScoredGameRoom> first = settings.getGameRooms().stream().filter(g -> g.getUuid().equals(gameRoomId)).findFirst();
    if (first.isPresent()) {
      IScoredGameRoom room = first.get();

      List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();
      for (Competition iScoredSubscription : iScoredSubscriptions) {
        if (iScoredSubscription.getUrl().equals(room.getUrl())) {
          competitionService.delete(iScoredSubscription.getId());
          LOG.info("Deleted iScored competition {}", iScoredSubscription.getName());
        }
      }
      settings.remove(room);
      preferencesService.savePreference(settings, true);
      LOG.info("Deleted {}", room);
      return true;
    }
    return false;
  }

  public void submitScore(@NonNull IScoredGame iScoredGame, Score newScore) {
    GameRoom gameRoom = IScored.getGameRoom(iScoredGame.getGameRoomUrl(), true);
    if (gameRoom != null) {
      String playerName = newScore.getPlayerInitials();
      if (newScore.getPlayer() != null) {
        playerName = newScore.getPlayer().getName();

        if (!StringUtils.isEmpty(newScore.getPlayer().getiScoredName())) {
          playerName = newScore.getPlayer().getiScoredName();
        }
      }
      IScoredResult result = IScored.submitScore(gameRoom, iScoredGame, playerName, newScore.getPlayerInitials(), newScore.getScore());
      SLOG.info(result.toString());

      if (Features.NOTIFICATIONS_ENABLED && result.isSent() && notificationSettings.isiScoredNotification()) {
        Game game = gameService.getGame(newScore.getGameId());
        File wheelImage = gameService.getWheelImage(game);
        Notification notification = NotificationFactory.createNotification(wheelImage,
            game.getGameDisplayName(), "An iScored highscore has been posted!",
            newScore.getPosition() + ". " + newScore.getPlayerInitials() + "\t" + newScore.getScore());
        notificationService.showNotification(notification);
      }
    }
    else {
      LOG.warn("No iScored game room found for {}", iScoredGame.getGameRoomUrl());
      SLOG.warn("No iScored game room found for " + iScoredGame.getGameRoomUrl());
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
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
