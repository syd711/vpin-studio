package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

abstract public class DefaultCompetitionChangeListener implements CompetitionChangeListener {

  /**
   * Checks if there are any augmented wheel icons that do not belong
   * to any competition anymore.
   */
  protected void runCheckedDeAugmentation(CompetitionService competitionService, GameService gameService, FrontendStatusService frontendStatusService) {
    List<Integer> competedGameIds = competitionService.getActiveCompetitions().stream().map(Competition::getGameId).collect(Collectors.toList());
    for (Integer competedGameId : competedGameIds) {
      Game game = gameService.getGame(competedGameId);
      if (game != null) {
        frontendStatusService.deAugmentWheel(game);
      }
    }
  }
}
