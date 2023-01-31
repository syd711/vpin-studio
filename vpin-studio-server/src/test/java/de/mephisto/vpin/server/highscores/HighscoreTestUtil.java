package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.PlayerService;

import java.util.Date;

public class HighscoreTestUtil {

  public static HighscoreChangeEvent createHighscoreChangeEvent(PlayerService playerService, Game game, double score, int pos, long serverId) {
    Score oldScore = new Score(new Date(), game.getId(), "MFA", playerService.getPlayerForInitials(serverId, "MFA"), String.valueOf(score), score, pos);
    Score newScore = new Score(new Date(), game.getId(), "MFA", null, String.valueOf((score + 1000)), score + 1000, pos);
    return new HighscoreChangeEvent(game, null, null, oldScore, newScore);
  }
}
