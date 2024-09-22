package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IScoredHighscoreChangeListener implements HighscoreChangeListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredHighscoreChangeListener.class);

  @Autowired
  private IScoredService iScoredService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private HighscoreService highscoreService;

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    Game game = event.getGame();
    Score newScore = event.getNewScore();

    if (event.getNewScore().getPlayer() == null) {
      LOG.info("Ignored iScored highscore change, because no player set for this score.");
      SLOG.info("Ignored iScored highscore change, because no player set for this score.");
      return;
    }

    try {
      Optional<Competition> optionalCompetition = competitionService.getIScoredSubscriptions().stream().filter(s -> s.getGameId() == game.getId()).findFirst();
      if (optionalCompetition.isPresent()) {
        Competition iScoredSubscription = optionalCompetition.get();
        String url = iScoredSubscription.getUrl();
        if (iScoredService.isIscoredGameRoomUrl(url)) {
          LOG.info("Emitting iScored game score to " + url);
          SLOG.info("Emitting iScored game score to " + url);
          iScoredService.submitScore(url, newScore, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
        }
        else {
          LOG.warn("The URL of " + iScoredSubscription + " (" + iScoredSubscription.getUrl() + ") is not a valid iScored URL.");
          SLOG.warn("The URL of " + iScoredSubscription + " (" + iScoredSubscription.getUrl() + ") is not a valid iScored URL.");
        }
      }
      else {
        LOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\"");
        SLOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\"");
      }
    }
    catch (Exception e) {
      LOG.error("Failed emitting iScored highscore: " + e.getMessage(), e);
      SLOG.error("Failed emitting iScored highscore: " + e.getMessage());
    }
  }


  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.ISCORED_ENABLED) {
      highscoreService.addHighscoreChangeListener(this);
    }
  }
}
