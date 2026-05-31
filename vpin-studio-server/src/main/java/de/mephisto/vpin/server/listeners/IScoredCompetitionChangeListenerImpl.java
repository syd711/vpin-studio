package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class IScoredCompetitionChangeListenerImpl extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private HighscoreService highscoreService;


  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.ISCORED.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game == null) {
        game = gameService.getGameByVpsTable(competition.getVpsTableId(), competition.getVpsTableVersionId());
      }

      if (game != null) {
        if (competition.isHighscoreReset()) {
          if (highscoreBackupService.backup(game) != null) {
            highscoreService.resetHighscore(game);
          }
        }
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionLifecycleService.addCompetitionChangeListener(this);
  }
}
