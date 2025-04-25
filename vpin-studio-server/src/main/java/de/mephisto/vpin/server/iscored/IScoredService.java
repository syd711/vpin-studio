package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.IScoredResult;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.competitions.Competition;
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

import java.util.List;

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

  public void submitTournamentScore(@NonNull Tournament tournament, @NonNull TournamentTable tournamentTable, @NonNull TableScore tableScore, @NonNull Account account) {
    if (!Features.ISCORED_ENABLED) {
      LOG.warn("iScored is not enabled");
      return;
    }
    String dashboardUrl = tournament.getDashboardUrl();
    if (!IScored.isIScoredGameRoomUrl(dashboardUrl)) {
      LOG.info("Not a tournament url for iScored: {}", dashboardUrl);
      return;
    }

    try {
      GameRoom gameRoom = IScored.getGameRoom(dashboardUrl, true);
      if (gameRoom != null) {
        if (!gameRoom.getSettings().isPublicScoreEnteringEnabled()) {
          LOG.warn("Cancelling iScored score submission, public score submissions are not enabled!");
          SLOG.warn("Cancelling iScored score submission, public score submissions are not enabled!");
          return;
        }

        String vpsTableId = tournamentTable.getVpsTableId();
        String vpsVersionId = tournamentTable.getVpsVersionId();

        IScoredGame gameRoomGame = gameRoom.getGameByVps(vpsTableId, vpsVersionId);
        if (gameRoomGame == null) {
          LOG.info("Skipped iScored score submission, because no game was found for " + tournament);
          SLOG.info("Skipped iScored score submission, because no game was found for " + tournament);
          return;
        }

        if (gameRoomGame.isDisabled()) {
          LOG.info("Skipped iScored score submission, because table " + gameRoomGame + " has disabled flag set.");
          SLOG.info("Skipped iScored score submission, because table " + gameRoomGame + " has disabled flag set.");
          return;
        }

        IScored.submitScore(gameRoom, gameRoomGame, account.getDisplayName(), account.getInitials(), tableScore.getScore());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load game room from " + tournament.getDashboardUrl() + ": " + e.getMessage(), e);
      SLOG.error("Failed to load game room from " + tournament.getDashboardUrl() + ": " + e.getMessage());
    }
  }

  public void submitScore(Competition iScoredSubscription, Score newScore) {
    String url = iScoredSubscription.getUrl();
    if (!IScored.isIScoredGameRoomUrl(url)) {
      LOG.warn("The URL of " + iScoredSubscription + " (" + url + ") is not a valid iScored URL.");
      SLOG.warn("The URL of " + iScoredSubscription + " (" + url + ") is not a valid iScored URL.");
      return;
    }

    LOG.info("Emitting iScored game score to " + url);
    SLOG.info("Emitting iScored game score to " + url);

    GameRoom gameRoom = IScored.getGameRoom(url, true);
    if (gameRoom != null) {
      String vpsTableId = iScoredSubscription.getVpsTableId();
      String vpsVersionId = iScoredSubscription.getVpsTableVersionId();
      IScoredGame iScoredGame = gameRoom.getGameByVps(vpsTableId, vpsVersionId);
      if (iScoredGame != null) {
        String playerName = newScore.getPlayer() != null ? newScore.getPlayer().getName() : newScore.getPlayerInitials();
        if (iScoredGame.isDisabled()) {
          LOG.info("Skipped iScored score submission, because table " + iScoredGame + " has disabled flag set.");
          SLOG.info("Skipped iScored score submission, because table " + iScoredGame + " has disabled flag set.");
          return;
        }

        IScoredResult result = IScored.submitScore(gameRoom, iScoredGame, playerName, newScore.getPlayerInitials(), newScore.getScore());
        SLOG.info(result.toString());
        if (Features.NOTIFICATIONS_ENABLED && result.isSent() && notificationSettings.isiScoredNotification()) {
          Game game = gameService.getGame(newScore.getGameId());

          Notification notification = NotificationFactory.createNotification(game.getWheelImage(),
              game.getGameDisplayName(), "An iScored highscore has been posted!",
              newScore.getPosition() + ". " + newScore.getPlayerInitials() + "\t" + newScore.getScore());
          notificationService.showNotification(notification);
        }
      }
      else {
        LOG.warn("Failed to get iScored game from game room (" + url + ") for VPS id '" + vpsTableId + "' and VPS version id '" + vpsVersionId + "'");
        SLOG.warn("Failed to get iScored game from game room (" + url + ") for VPS id '" + vpsTableId + "' and VPS version id '" + vpsVersionId + "', check is tagged with the correct URL.");
        List<IScoredGame> games = gameRoom.getGames();
        SLOG.info("Game Room Games:");
        for (IScoredGame game : games) {
          SLOG.info(" - " + game.getName() + "[" + String.join(", ", game.getTags()) + "] (matches VPS ids: " + game.matches(vpsTableId, vpsVersionId) + ")");
        }
      }
    }
    else {
      LOG.warn("No iScored game room found for " + url);
      SLOG.warn("No iScored game room found for " + url);
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
