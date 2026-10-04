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
import de.mephisto.vpin.restclient.competitions.IScoredSyncModel;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.iscored.IScoredCompetitionSynchronizer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ServerMessages;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

@Service
public class IScoredService implements PreferenceChangedListener, InitializingBean, DisposableBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredService.class);

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Autowired
  private IScoredCompetitionSynchronizer iScoredCompetitionSynchronizer;

  private NotificationSettings notificationSettings;

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "iScored Sync Scheduler");
    thread.setDaemon(true);
    return thread;
  });

  /**
   * game room uuid -> scheduled synchronization
   */
  private final Map<String, ScheduledFuture<?>> scheduledSyncs = new HashMap<>();

  /**
   * uuids of game rooms whose synchronization is postponed because an emulator is running
   */
  private final Set<String> postponedSyncs = new HashSet<>();

  private static final int EMULATOR_RETRY_SECONDS = 60;

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

        if (!StringUtils.isEmpty(newScore.getPlayer().getCompetitionName())) {
          playerName = newScore.getPlayer().getCompetitionName();
        }
      }
      IScoredResult result = IScored.submitScore(gameRoom, iScoredGame, playerName, newScore.getPlayerInitials(), newScore.getScore());
      SLOG.info(result.toString());

      if (Features.NOTIFICATIONS_ENABLED && result.isSent() && notificationSettings.isIscoredNotification()) {
        Game game = gameService.getGame(newScore.getGameId());
        File wheelImage = gameService.getWheelImage(game);
        Notification notification = NotificationFactory.createNotification(wheelImage,
            game.getGameDisplayName(), ServerMessages.get("notification.iscored.posted", ServerMessages.resolveLocalLocale()),
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
    else if (propertyName.equals(PreferenceNames.ISCORED_SETTINGS)) {
      rescheduleSynchronization();
    }
  }

  /**
   * (Re-)creates the periodic synchronization jobs, one for each game room with a configured interval.
   */
  private synchronized void rescheduleSynchronization() {
    scheduledSyncs.values().forEach(f -> f.cancel(false));
    scheduledSyncs.clear();

    IScoredSettings settings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    if (settings == null || !settings.isEnabled()) {
      return;
    }

    for (IScoredGameRoom gameRoom : settings.getGameRooms()) {
      int interval = gameRoom.getSyncIntervalMinutes();
      if (!gameRoom.isSynchronize() || interval <= 0) {
        continue;
      }

      String uuid = gameRoom.getUuid();
      scheduledSyncs.put(uuid, scheduler.scheduleWithFixedDelay(() -> runScheduledSync(uuid), interval, interval, TimeUnit.MINUTES));
      LOG.info("Scheduled iScored synchronization of {} every {} minutes", gameRoom.getUrl(), interval);
    }
  }

  private void runScheduledSync(String gameRoomUuid) {
    try {
      // never synchronize while a game is played, retry until the emulator has been closed
      if (SystemService.isPinballEmulatorRunning()) {
        synchronized (postponedSyncs) {
          if (postponedSyncs.add(gameRoomUuid)) {
            LOG.info("Postponing iScored synchronization, an emulator is running.");
            scheduler.schedule(() -> {
              synchronized (postponedSyncs) {
                postponedSyncs.remove(gameRoomUuid);
              }
              runScheduledSync(gameRoomUuid);
            }, EMULATOR_RETRY_SECONDS, TimeUnit.SECONDS);
          }
        }
        return;
      }

      // always resolve the current settings, the game room may have been changed or removed
      IScoredSettings settings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      Optional<IScoredGameRoom> room = settings.getGameRooms().stream().filter(g -> g.getUuid().equals(gameRoomUuid)).findFirst();
      if (settings.isEnabled() && room.isPresent() && room.get().isSynchronize()) {
        IScoredSyncModel model = new IScoredSyncModel();
        model.setGameRoom(room.get());
        model.setInvalidate(true);
        iScoredCompetitionSynchronizer.synchronize(model);
      }
    }
    catch (Exception e) {
      LOG.error("Scheduled iScored synchronization failed: {}", e.getMessage(), e);
    }
  }

  @Override
  public void destroy() {
    scheduler.shutdownNow();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);
    rescheduleSynchronization();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
