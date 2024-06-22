package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IScoredService implements PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredService.class);

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameService gameService;

  private NotificationSettings notificationSettings;

  public void submitTournamentScore(@NonNull Tournament tournament, @NonNull TournamentTable tournamentTable, @NonNull TableScore tableScore) {
    if (!Features.ISCORED_ENABLED) {
      LOG.warn("iScored is not enabled");
      return;
    }

    try {
      String dashboardUrl = tournament.getDashboardUrl();
      if (dashboardUrl != null && dashboardUrl.contains("iscored")) {
        GameRoom gameRoom = IScored.loadGameRoom(dashboardUrl);
        if (gameRoom != null) {
          if (!gameRoom.getSettings().isPublicScoresEnabled()) {
            LOG.warn("Cancelling iScord score submission, public score submission is not enabled!");
            return;
          }

          String vpsTableId = tournamentTable.getVpsTableId();
          String vpsVersionId = tournamentTable.getVpsVersionId();

          IScoredGame gameRoomGame = gameRoom.getGameByVps(vpsTableId, vpsVersionId);
          if (gameRoomGame == null) {
            LOG.info("Skipped iScored score submission, because no game was found for " + tournament);
            return;
          }

          if (gameRoomGame.isDisabled()) {
            LOG.info("Skipped iScored score submission, because table " + gameRoomGame + " has disabled flag set.");
            return;
          }

          IScored.submitScore(gameRoom, gameRoomGame, tableScore.getPlayerName(), tableScore.getPlayerInitials(), tableScore.getScore());
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load game room from " + tournament.getDashboardUrl() + ": " + e.getMessage(), e);
    }
  }

  public boolean isIscoredGameRoomUrl(String dashboardUrl) {
    return dashboardUrl.toLowerCase().contains("iscored.info") && dashboardUrl.contains("user=") && dashboardUrl.toLowerCase().contains("mode=public");
  }

  public void submitScore(String url, Score newScore, String vpsTableId, String vpsVersionId) {
    GameRoom gameRoom = IScored.getGameRoom(url);
    if (gameRoom != null) {
      IScoredGame iScoredGame = gameRoom.getGameByVps(vpsTableId, vpsVersionId);
      if (iScoredGame != null) {
        String playerName = newScore.getPlayer() != null ? newScore.getPlayer().getName() : newScore.getPlayerInitials();
        if (iScoredGame.isDisabled()) {
          LOG.info("Skipped iScored score submission, because table " + iScoredGame + " has disabled flag set.");
          return;
        }

        IScored.submitScore(gameRoom, iScoredGame, playerName, newScore.getPlayerInitials(), (long) newScore.getNumericScore());
        if (Features.NOTIFICATIONS_ENABLED && notificationSettings.isiScoredNotification()) {
          Game game = gameService.getGame(newScore.getGameId());
          Notification notification = new Notification();
          notification.setImage(game.getWheelImage());
          notification.setTitle1(game.getGameDisplayName());
          notification.setTitle2("An iScored highscore has been posted!");
          notification.setTitle3(newScore.getPosition() + ". " + newScore.getPlayerInitials() + "\t" + newScore.getScore());
          notificationService.showNotification(notification);
        }
      }
      else {
        LOG.warn("Failed to get iScored game from game room (" + url + ") for VPS id '" + vpsTableId + "'");
      }
    }
    else {
      LOG.warn("No iScored game room found for " + url);
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
  }
}
