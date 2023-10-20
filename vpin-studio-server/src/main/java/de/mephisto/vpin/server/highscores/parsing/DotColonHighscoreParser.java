package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.highscores.parsing.HighscoreParser.toNumericScore;

public class DotColonHighscoreParser implements CustomParser {

  @Nullable
  public Score parse(@NotNull PlayerService playerService, @NonNull Date createdAt, @NotNull String line, int gameId, long serverId) {
    if(line.indexOf(".:") != 1) {
      return null;
    }

    List<String> scoreLineSegments = Arrays.asList(line.split(":"));
    if(scoreLineSegments.size() != 3) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    String pos = scoreLineSegments.get(0).substring(0, 1);
    builder.append(pos);


    String initials = scoreLineSegments.get(1);
    String score = scoreLineSegments.get(2);
    Player player = playerService.getPlayerForInitials(serverId, initials);
    double v = toNumericScore(score);
    if (v == -1) {
      return null;
    }
    return new Score(createdAt, gameId, initials, player, score, v, -1);
  }
}
