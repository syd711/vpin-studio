package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompetitionChangeListenerImpl extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionChangeListenerImpl.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    Game game = gameService.getGame(competition.getGameId());
    boolean active = competition.isActive();

    //the data has already been saved, check other changes, like the badge
    if (game != null && active) {
      if (competition.getBadge() != null) {
        frontendStatusService.augmentWheel(game, competition.getBadge());
      }
    }
    runCheckedDeAugmentation(competitionService, gameService, frontendStatusService);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionLifecycleService.addCompetitionChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
