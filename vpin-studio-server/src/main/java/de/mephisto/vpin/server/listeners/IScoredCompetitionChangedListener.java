package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IScoredCompetitionChangedListener extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredCompetitionChangedListener.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private HighscoreService highscoreService;

  @Override
  public void competitionCreated(@NotNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    if (game == null) {
      LOG.info("No game found for id " + competition.getGameId() + ", the iScord subscription was created without an existing table.");
      return;
    }

    if (competition.getBadge() != null && competition.isActive()) {
      popperService.augmentWheel(game, competition.getBadge());
    }
  }

  @Override
  public void competitionDeleted(@NotNull Competition competition) {
    super.runCheckedDeAugmentation(competitionService, gameService, popperService);
  }

  @Override
  public void afterPropertiesSet() {
    competitionService.addCompetitionChangeListener(this);
  }
}
