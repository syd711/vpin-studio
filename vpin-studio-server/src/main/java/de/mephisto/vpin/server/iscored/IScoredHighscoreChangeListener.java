package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.players.PlayerService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    new Thread(() -> {
      Thread.currentThread().setName("iScored Highscore ChangeListener Thread");

      Game game = event.getGame();
      Score newScore = event.getNewScore();

      try {
        List<Competition> iScoredSubscriptions = competitionService.getIScoredSubscriptions();
        for (Competition iScoredSubscription : iScoredSubscriptions) {
          int gameId = iScoredSubscription.getGameId();
          if (gameId == game.getId()) {
            String url = iScoredSubscription.getUrl();
            if (iScoredService.isIscoredGameRoomUrl(url)) {
              iScoredService.submitScore(url, newScore, iScoredSubscription.getVpsTableId(), iScoredSubscription.getVpsTableVersionId());
            }
            else {
              LOG.warn("The URL of " + iScoredSubscription + " (" + iScoredSubscription.getUrl() + ") is not a valid iScored URL.");
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to submit iScored highscore: " + e.getMessage(), e);
      }
    }).start();
  }


  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    highscoreService.addHighscoreChangeListener(this);
  }
}
