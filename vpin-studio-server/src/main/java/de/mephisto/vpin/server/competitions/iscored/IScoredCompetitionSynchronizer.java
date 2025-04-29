package de.mephisto.vpin.server.competitions.iscored;

import de.mephisto.vpin.commons.fx.Features;
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
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IScoredCompetitionSynchronizer implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, TableStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredCompetitionSynchronizer.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferencesService;

  private List<Game> knownGames = null;

  public IScoredSyncModel synchronize(IScoredSyncModel syncModel) {
    if (syncModel.getGame() != null) {
      LOG.info("--- ------- iScored Sync (" + syncModel.getGame().getName() + ")-----------------");
    }
    else {
      LOG.info("--- ------- iScored Sync (" + syncModel.getiScoredGameRoom().getUrl() + ")-----------------");
    }


    GameRoom gameRoom = IScored.getGameRoom(syncModel.getiScoredGameRoom().getUrl(), syncModel.isInvalidate());
    List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();

    //cache the known games for the next sync
    if (syncModel.isInvalidate() || knownGames == null) {
      knownGames = gameService.getKnownGames(-1);
    }

    //clean up invalid competitions
    if (syncModel.getiScoredGameRoom().isSynchronize() || (syncModel.getGame() != null && syncModel.isManualSubscription())) {
      List<Competition> updated = synchronizeExistingCompetitions(syncModel, iScoredSubscriptions, knownGames);
      if (syncModel.getGame() != null) {
        synchronizeGame(syncModel, syncModel.getGame(), updated, knownGames);
        LOG.info("Synchronization finished: {} ({})", syncModel.getGame().getName(), gameRoom.getUrl());
      }
      else {
        List<IScoredGame> games = gameRoom.getGames();
        for (IScoredGame game : games) {
          synchronizeGame(syncModel, game, updated, knownGames);
          LOG.info("Synchronization finished: {} ({})", game.getName(), gameRoom.getUrl());
        }
      }
    }

    LOG.info("--- ------- /iScored Sync (" + syncModel.getiScoredGameRoom().getUrl() + ")-----------------");
    return syncModel;
  }

  /**
   * Checks if the existing iScored subscriptions are still valid.
   * If no game is found, the subscription is deleted too.
   *
   * @param syncModel
   * @param iScoredSubscriptions
   * @param knownGames
   */
  private List<Competition> synchronizeExistingCompetitions(IScoredSyncModel syncModel, List<Competition> iScoredSubscriptions, List<Game> knownGames) {
    long start = System.currentTimeMillis();
    List<Competition> subs = new ArrayList<>(iScoredSubscriptions);
    for (Competition iScoredSubscription : subs) {
      GameRoom gameRoom = IScored.getGameRoom(iScoredSubscription.getUrl(), false);
      if (gameRoom == null) {
        competitionService.delete(iScoredSubscription.getId());
        iScoredSubscriptions.remove(iScoredSubscription);
        LOG.info("Deleted competition {} because no matching iScored Game Room found for URL {}", iScoredSubscription, iScoredSubscription.getUrl());
        continue;
      }

      IScoredGame game = gameRoom.getGameByVps(iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());

      //no matching game found in the game room, so it has been removed
      if (game == null) {
        competitionService.delete(iScoredSubscription.getId());
        iScoredSubscriptions.remove(iScoredSubscription);
        LOG.info("Deleted competition {} because no matching Game Room game found for VPS table/version: {}/{}", iScoredSubscription, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
        continue;
      }

      //get game depending on if versions are enabled.
      Game gameByVpsTable = null;
      if (game.isAllVersionsEnabled()) {
        gameByVpsTable = gameService.getGameByVpsTable(knownGames, iScoredSubscription.getVpsTableId(), null);
      }
      else {
        gameByVpsTable = gameService.getGameByVpsTable(knownGames, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
      }

      //no matching game exists anymore
      if (gameByVpsTable == null) {
        competitionService.delete(iScoredSubscription.getId());
        iScoredSubscriptions.remove(iScoredSubscription);
        LOG.info("Deleted competition {} because no matching table found for VPS table/version: {}/{}", iScoredSubscription, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
      }
    }
    LOG.info("Existing competitions sync took {}ms", (System.currentTimeMillis() - start));
    return iScoredSubscriptions;
  }

  private void synchronizeGame(IScoredSyncModel syncModel, IScoredGame game, List<Competition> iScoredSubscriptions, List<Game> knownGames) {
    if (!game.isVpsTagged()) {
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
      //no matching table available.
      return;
    }

    syncModel.getUpdatedGameIds().addAll(matches.stream().map(g -> g.getId()).collect(Collectors.toList()));

    IScoredGameRoom iScoredGameRoom = syncModel.getiScoredGameRoom();

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
        List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
        for (IScoredGameRoom gameRoom : gameRooms) {
          if (!gameRoom.isSynchronize()) {
            LOG.info("Skipped initial sync of Game Room " + gameRoom.getUrl() + ", sync is not enabled.");
            continue;
          }

          IScoredSyncModel syncModel = new IScoredSyncModel();
          syncModel.setiScoredGameRoom(gameRoom);
          synchronize(syncModel);
        }

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
      List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
      for (IScoredGameRoom gameRoom : gameRooms) {
        IScoredSyncModel model = new IScoredSyncModel();
        model.setInvalidate(true);
        model.setiScoredGameRoom(gameRoom);
        synchronize(model);
      }
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
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
