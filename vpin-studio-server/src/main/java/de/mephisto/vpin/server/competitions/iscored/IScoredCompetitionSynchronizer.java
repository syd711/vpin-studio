package de.mephisto.vpin.server.competitions.iscored;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.IScoredSyncModel;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IScoredCompetitionSynchronizer implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredCompetitionSynchronizer.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionService competitionService;

  private List<Game> knownGames = null;


  public boolean synchronize(IScoredSyncModel syncModel) {
    LOG.info("----------------------------- iScored Sync ------------------------------------------------------------");
    GameRoom gameRoom = IScored.getGameRoom(syncModel.getiScoredGameRoom().getUrl(), syncModel.isInvalidate());
    List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();

    //cache the known games for the next sync
    if (syncModel.isInvalidate() || knownGames == null) {
      knownGames = gameService.getKnownGames(-1);
    }

    //clean up invalid competitions
    if (syncModel.getiScoredGameRoom().

        isSynchronize()) {
      List<Competition> updated = synchronizeExistingCompetitions(iScoredSubscriptions, knownGames);

      if (syncModel.getGame() != null) {
        synchronizeGame(syncModel, syncModel.getGame(), updated, knownGames);
      }
      else {
        List<IScoredGame> games = gameRoom.getGames();
        for (IScoredGame game : games) {
          synchronizeGame(syncModel, game, updated, knownGames);
        }
      }
      return true;
    }

    LOG.info("----------------------------- /iScored Sync -----------------------------------------------------------");
    return false;
  }

  /**
   * Checks if the existing iScored subscriptions are still valid.
   * If no game is found, the subscription is deleted too.
   *
   * @param iScoredSubscriptions
   * @param knownGames
   */
  private List<Competition> synchronizeExistingCompetitions(List<Competition> iScoredSubscriptions, List<Game> knownGames) {
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
    Optional<Competition> matchingCompetition = iScoredSubscriptions.stream().filter(c -> c.getVpsTableId().equals(game.getVpsTableId()) && c.getVpsTableVersionId().equals(game.getVpsTableVersionId())).findFirst();
    if (matchingCompetition.isPresent()) {
      return;
    }

    if (!game.isVpsTagged()) {
      return;
    }

    Game gameByVpsTable = gameService.getGameByVpsTable(knownGames, game.getVpsTableId(), game.getVpsTableVersionId());
    if (gameByVpsTable == null) {
      //no matching table available.
      return;
    }

    IScoredGameRoom iScoredGameRoom = syncModel.getiScoredGameRoom();

    Competition competition = new Competition();
    competition.setType(CompetitionType.ISCORED.name());
    competition.setName("iScored Subscription for " + game.getName());
    competition.setHighscoreReset(iScoredGameRoom.isScoreReset());
    competition.setBadge(iScoredGameRoom.getBadge());
    competition.setVpsTableId(game.getVpsTableId());
    competition.setVpsTableVersionId(game.getVpsTableVersionId());
    competition.setUrl(syncModel.getiScoredGameRoom().getUrl());
    competition.setUuid(UUID.randomUUID().toString());
    competitionService.save(competition);

    LOG.info("Create new iScored subscription: {}", competition.getName());
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
