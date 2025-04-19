package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
  public void highscoreChanged(@NonNull HighscoreChangeEvent event) {
    Game game = event.getGame();
    Score newScore = event.getNewScore();

    if (event.getNewScore().getPlayer() == null) {
      LOG.info("Ignored iScored highscore change, because no player set for this score.");
      SLOG.info("Ignored iScored highscore change, because no player set for this score.");
      return;
    }

    try {
      List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();
      Optional<Competition> optionalCompetition = iScoredSubscriptions.stream().filter(s -> s.getGameId() == game.getId()).findFirst();
      if (optionalCompetition.isPresent()) {
        Competition iScoredSubscription = optionalCompetition.get();
        iScoredService.submitScore(iScoredSubscription, newScore);
      }
      else {
        if (StringUtils.isEmpty(game.getExtTableId())) {
          LOG.info("No iScored update sent, because table \"" + game.getGameDisplayName() + "\" has no VPS table id. " + iScoredSubscriptions.size() + " competitions have been checked.");
          SLOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\" (No VPS table id found). " + iScoredSubscriptions.size() + " competitions have been checked.");
        }
        else if (StringUtils.isEmpty(game.getExtTableVersionId())) {
          LOG.info("No iScored update sent, because table \"" + game.getGameDisplayName() + "\" has no VPS table version id. " + iScoredSubscriptions.size() + " have been checked.");
          SLOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\" (No VPS table version id found). " + iScoredSubscriptions.size() + " competitions have been checked.");
        }
        else {
          String vpsTableUrl = VPS.getVpsTableUrl(game.getExtTableId());
          for (Competition iScoredSubscription : iScoredSubscriptions) {
            String url = iScoredSubscription.getUrl();
            //check if there is at least a matching game
            if (!StringUtils.isEmpty(url) && url.startsWith(vpsTableUrl)) {
              LOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\", but for VPS game " + url + ". " + iScoredSubscriptions.size() + " competitions have been checked.");
              SLOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\", but for VPS game " + url + ". " + iScoredSubscriptions.size() + " competitions have been checked.");
              return;
            }
          }

          LOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\". " + iScoredSubscriptions.size() + " competitions have been checked.");
          SLOG.info("No iScored update sent, because there is no iScored subscription for table \"" + game.getGameDisplayName() + "\". " + iScoredSubscriptions.size() + " competitions have been checked.");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed emitting iScored highscore: " + e.getMessage(), e);
      SLOG.error("Failed emitting iScored highscore: " + e.getMessage());
    }
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
