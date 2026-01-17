package de.mephisto.vpin.server.competitions.iscored;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.IScoredSyncModel;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

@Service
public class IScoredCompetitionSynchronizer implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, TableStatusChangeListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredCompetitionSynchronizer.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferencesService;

  public boolean synchronizeGameRooms() {
    LOG.info("---------- Starting iScored Sync -----------------");
    List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();
    IScoredSettings iScoredSettings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    List<Game> knownGames = gameService.getKnownGames(-1);

    synchronizeExistingCompetitions(iScoredSubscriptions, knownGames, iScoredSettings);

    if (iScoredSettings.isEnabled()) {
      List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
      for (IScoredGameRoom gameRoom : gameRooms) {
        if (gameRoom.isSynchronize()) {
          IScoredSyncModel model = new IScoredSyncModel();
          model.setiScoredGameRoom(gameRoom);
          model.setInvalidate(true);
          synchronize(model, knownGames);
        }
      }
    }
    return true;
  }

  public IScoredSyncModel synchronize(IScoredSyncModel syncModel) {
    List<Game> knownGames = gameService.getKnownGames(-1);
    return synchronize(syncModel, knownGames);
  }

  private IScoredSyncModel synchronize(IScoredSyncModel syncModel, List<Game> knownGames) {
    if (syncModel.getGame() != null) {
      LOG.info("--- ------- iScored Sync (" + syncModel.getGame().getName() + ")-----------------");
    }
    else {
      LOG.info("--- ------- iScored Sync (" + syncModel.getiScoredGameRoom().getUrl() + ")-----------------");
    }

    GameRoom gameRoom = IScored.getGameRoom(syncModel.getiScoredGameRoom().getUrl(), syncModel.isInvalidate());
    if (gameRoom != null) {
      List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();

      //clean up invalid competitions
      if (syncModel.getiScoredGameRoom().isSynchronize() || (syncModel.getGame() != null && syncModel.isManualSubscription())) {
        if (syncModel.getGame() != null) {
          synchronizeGame(syncModel, syncModel.getGame(), iScoredSubscriptions, knownGames);
          LOG.info("Synchronization finished: {} ({})", syncModel.getGame().getName(), gameRoom.getUrl());
        }
        else {
          List<IScoredGame> games = gameRoom.getGames();
          for (IScoredGame game : games) {
            synchronizeGame(syncModel, game, iScoredSubscriptions, knownGames);
            LOG.info("Synchronization finished: {} ({})", game.getName(), gameRoom.getUrl());
          }
        }
      }
    }
    else {
      LOG.info("Cancelled sync, game room could not be loaded.");
    }

    LOG.info("--- ------- /iScored Sync (" + syncModel.getiScoredGameRoom().getUrl() + ")-----------------");
    return syncModel;
  }

  /**
   * Checks if the existing iScored subscriptions are still valid.
   * If no game is found, the subscription is deleted too.
   *
   * @param iScoredSubscriptions
   * @param knownGames
   * @param iScoredSettings
   */
  private void synchronizeExistingCompetitions(List<Competition> iScoredSubscriptions, List<Game> knownGames, IScoredSettings iScoredSettings) {
    long start = System.currentTimeMillis();
    List<Competition> subs = new ArrayList<>(iScoredSubscriptions);
    for (Competition iScoredSubscription : subs) {
      if (!iScoredSettings.isEnabled()) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because iScored is not enabled.", iScoredSubscription);
        continue;
      }

      Optional<IScoredGameRoom> anyGameRoomMatch = iScoredSettings.getGameRooms().stream().filter(g -> g.getUrl().equals(iScoredSubscription.getUrl())).findAny();

      //delete if the game room does not exists anymore
      if (anyGameRoomMatch.isEmpty()) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because no matching iScored Game Room found for URL {}", iScoredSubscription, iScoredSubscription.getUrl());
        continue;
      }

      //delete if the game room could not be loaded
      GameRoom gameRoom = IScored.getGameRoom(iScoredSubscription.getUrl(), false);
      if (gameRoom == null) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because no matching iScored Game Room found for URL {}", iScoredSubscription, iScoredSubscription.getUrl());
        continue;
      }

      IScoredGame iScoredGame = gameRoom.getGameByVps(iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());

      //no matching game found in the game room, so it has been removed
      if (iScoredGame == null) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because no matching Game Room game found for VPS table/version: {}/{}", iScoredSubscription, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
        continue;
      }

      IScoredGameRoom iScoredGameRoom = anyGameRoomMatch.get();
      if (iScoredGame.isGameHidden() && iScoredGameRoom.isIgnoreHidden()) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because the matching game is hidden", iScoredSubscription);
        continue;
      }

      if (iScoredGame.isAllVersionsEnabled() && !StringUtils.isEmpty(iScoredSubscription.getVpsTableVersionId())) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because the it has a VPS version id, but all versions are enabled.", iScoredSubscription);
        continue;
      }

      //get game depending on if versions are enabled.
      Game gameByVpsTable = null;
      if (iScoredGame.isAllVersionsEnabled()) {
        gameByVpsTable = gameService.getGameByVpsTable(knownGames, iScoredSubscription.getVpsTableId(), null);
      }
      else {
        gameByVpsTable = gameService.getGameByVpsTable(knownGames, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
      }

      //no matching game exists anymore
      if (gameByVpsTable == null) {
        deleteSubscription(iScoredSubscriptions, iScoredSubscription);
        LOG.info("Deleted competition {} because no matching table found for VPS table/version: {}/{}", iScoredSubscription, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
      }
    }
    LOG.info("Existing competitions sync took {}ms", (System.currentTimeMillis() - start));
  }

  private void deleteSubscription(List<Competition> iScoredSubscriptions, Competition iScoredSubscription) {
    competitionService.delete(iScoredSubscription.getId());
    iScoredSubscriptions.remove(iScoredSubscription);
  }

  /**
   * Checks if a competition exists for the given game.
   * Invalid competitions have been cleaned up already at this point.
   *
   * @param syncModel
   * @param game
   * @param iScoredSubscriptions
   * @param knownGames
   */
  private void synchronizeGame(IScoredSyncModel syncModel, IScoredGame game, List<Competition> iScoredSubscriptions, List<Game> knownGames) {
    if (!game.isVpsTagged()) {
      LOG.info("Skipped synchronization of iScored game \"{}\": Game is not VPS tagged.", game.getName());
      return;
    }

    IScoredGameRoom iScoredGameRoom = syncModel.getiScoredGameRoom();
    if (game.isGameHidden() && iScoredGameRoom.isIgnoreHidden()) {
      LOG.info("Skipped synchronization of iScored game \"{}\": Game is hidden and ignored for synchronization.", game.getName());
      return;
    }

    //check if there is an existing competition
    for (Competition c : iScoredSubscriptions) {
      if (game.matches(c.getVpsTableId(), c.getVpsTableVersionId())) {
        return;
      }
    }

    List<Game> matches = null;
    if (game.isAllVersionsEnabled()) {
      matches = gameService.getGamesByVpsTableId(knownGames, game.getVpsTableId(), null);
    }
    else {
      matches = gameService.getGamesByVpsTableId(knownGames, game.getVpsTableId(), game.getVpsTableVersionId());
    }

    if (matches.isEmpty()) {
      LOG.info("Skipped synchronization of iScored game \"{}\": No local game found that matches this VPS settings (all versions enabled: {}).", game.getName(), game.isAllVersionsEnabled());
      return;
    }

    Competition competition = new Competition();
    competition.setType(CompetitionType.ISCORED.name());
    competition.setName("iScored Subscription for " + game.getName());
    competition.setHighscoreReset(iScoredGameRoom.isScoreReset());
    competition.setBadge(iScoredGameRoom.getBadge());
    competition.setVpsTableId(game.getVpsTableId());
    competition.setVpsTableVersionId(game.isAllVersionsEnabled() ? null : game.getVpsTableVersionId());
    competition.setUrl(syncModel.getiScoredGameRoom().getUrl());
    competition.setUuid(UUID.randomUUID().toString());
    competitionService.save(competition);

    LOG.info("Create new iScored subscription: {}", competition.getName());
  }


  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    IScoredSettings iScoredSettings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    if (Features.ISCORED_ENABLED && iScoredSettings.isEnabled()) {
      new Thread(() -> {
        long start = System.currentTimeMillis();
        Thread.currentThread().setName("IScored Initial Sync");
        LOG.info("----------------------------- Initial iScored Sync --------------------------------------------------");
        synchronizeGameRooms();
        LOG.info("----------------------------- /Initial iScored Sync -------------------------------------------------");
        LOG.info("Initial sync finished, took {}ms", (System.currentTimeMillis() - start));
      }).start();
    }
  }


  // ----------------------------- Table Status Changes ----------------------------------------------------------------
  @Override
  public void tableLaunched(TableStatusChangedEvent event) {

  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    IScoredSettings iScoredSettings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    if (iScoredSettings.isEnabled()) {
      LOG.info("Running iScored game room sync after table exit.");
      synchronizeGameRooms();
    }
  }

  @Override
  public int getPriority() {
    //we need a higher priority here since we need to run the sync before the highscore change event firing.
    return 100;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    frontendStatusService.addTableStatusChangeListener(this);
    preferencesService.addChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.ISCORED_SETTINGS.equals(propertyName)) {
      synchronizeGameRooms();
    }
  }
}
