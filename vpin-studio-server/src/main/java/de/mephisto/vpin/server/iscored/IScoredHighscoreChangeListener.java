package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.util.ArrayList;
import java.util.List;

@Service
public class IScoredHighscoreChangeListener implements HighscoreChangeListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredHighscoreChangeListener.class);

  @Autowired
  private IScoredService iScoredService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

  @Override
  public void highscoreChanged(@NonNull HighscoreChangeEvent event) {
    IScoredSettings iScoredSettings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    if (!iScoredSettings.isEnabled()) {
      LOG.info("Skipped iScored updates, it is not enabled.");
      SLOG.info("Skipped iScored updates, it is not enabled.");
      return;
    }

    Game game = event.getGame();
    Score newScore = event.getNewScore();
    if (event.getNewScore().getPlayer() == null) {
      LOG.info("Ignored iScored highscore change, because no player set for this score.");
      SLOG.info("Ignored iScored highscore change, because no player set for this score.");
      return;
    }


    List<IScoredGame> gameRoomGamesForTable = new ArrayList<>();
    List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
    for (IScoredGameRoom gameRoom : gameRooms) {
      //a reload is not required, since the synchronizer will take care of that
      GameRoom gr = IScored.getGameRoom(gameRoom.getUrl(), false);
      List<IScoredGame> games = gr.getGames();
      for (IScoredGame iScoredGame : games) {
        if (!iScoredGame.isVpsTagged()) {
          continue;
        }

        if (iScoredGame.matches(game.getExtTableId(), game.getExtTableVersionId())) {
          if (iScoredGame.isGameLocked() || iScoredGame.isDisabled()) {
            LOG.info("Found matching game room game \"" + iScoredGame.getName() + "\", but it is disabled or locked.");
            SLOG.info("Found matching game room game \"" + iScoredGame.getName() + "\", but it is disabled or locked.");
            continue;
          }
          gameRoomGamesForTable.add(iScoredGame);
        }
      }
    }

    if (gameRoomGamesForTable.isEmpty()) {
      LOG.info("Ignored iScored highscore change, because no game room games found.");
      SLOG.info("Ignored iScored highscore change, because no game room games found.");
      return;
    }


    try {
      List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();
      for (IScoredGame iScoredGame : gameRoomGamesForTable) {
        boolean isCompetitionAvailable = filterForMatchingCompetitions(iScoredSubscriptions, iScoredGame);
        if (!isCompetitionAvailable) {
          LOG.info("Found matching game room game \"" + iScoredGame.getName() + "\", but no matching iScored competition was created for them.");
          SLOG.info("Found matching game room game \"" + iScoredGame.getName() + "\", but no matching iScored competition was created for them.");
          continue;
        }

        iScoredService.submitScore(iScoredGame, newScore);
      }
    }
    catch (Exception e) {
      LOG.error("Failed emitting iScored highscore: " + e.getMessage(), e);
      SLOG.error("Failed emitting iScored highscore: " + e.getMessage());
    }
  }

  /**
   * We have the list of eligible game room game. Check if there is a matching subscription, only post then.
   */
  private boolean filterForMatchingCompetitions(List<Competition> iScoredSubscriptions, IScoredGame iScoredGame) {
    for (Competition c : iScoredSubscriptions) {
      if (iScoredGame.matches(c.getVpsTableId(), c.getVpsTableVersionId())) {
        return true;
      }
    }
    return false;
  }


  @Override
  public void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore) {

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.ISCORED_ENABLED) {
      highscoreService.addHighscoreChangeListener(this);
    }
  }
}
