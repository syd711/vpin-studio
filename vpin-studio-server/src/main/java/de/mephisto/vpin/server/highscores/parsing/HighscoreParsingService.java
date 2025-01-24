package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * e.g.:
 * <p>
 * CANNON BALL CHAMPION
 * TEX - 50
 * <p>
 * GRAND CHAMPION
 * RRR      60.000.000
 * <p>
 * HIGHEST SCORES
 * 1) POP      45.000.000
 * 2) LTD      40.000.000
 * 3) ROB      35.000.000
 * 4) ZAB      30.000.000
 * <p>
 * PARTY CHAMPION
 * PAB      20.000.000
 */
@Service
public class HighscoreParsingService {

  @Autowired
  private PlayerService playerService;

  @Autowired
  private SystemService systemService;

  @NonNull
  public List<Score> parseScores(@NonNull Date createdAt, @NonNull String raw, @Nullable Game game, long serverId) {
    List<Score> scores = ScoreListFactory.create(raw, createdAt, game, systemService.getScoringDatabase());
    for (Score score : scores) {
      Player player = playerService.getPlayerForInitials(serverId, score.getPlayerInitials());
      score.setPlayer(player);

      if (score.getPlayerInitials().equals("???") && score.getNumericScore() > 0) {
        Player admin = playerService.getAdminPlayer();
        if (admin != null) {
          score.setPlayer(admin);
          score.setPlayerInitials(admin.getInitials());
        }
      }
    }

    return scores;
  }
}
