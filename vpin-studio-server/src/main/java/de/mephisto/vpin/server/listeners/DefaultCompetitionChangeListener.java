package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;

import java.util.List;
import java.util.stream.Collectors;

abstract public class DefaultCompetitionChangeListener implements CompetitionChangeListener {

  /**
   * Checks if there are any augmented wheel icons that do not belong
   * to any competition anymore.
   */
  protected void runCheckedDeAugmentation(CompetitionService competitionService, GameService gameService, FrontendStatusService frontendStatusService) {
    List<Integer> competedGameIds = competitionService.getFinishedByDateCompetitions().stream().map(Competition::getGameId).toList();
    for (Integer competedGameId : competedGameIds) {
      Game game = gameService.getGame(competedGameId);
      if (game != null) {
        frontendStatusService.deAugmentWheel(game);
      }
    }
  }
}
