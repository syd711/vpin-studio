package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class HighscoreParser {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreParser.class);

  @Autowired
  private PlayerService playerService;

  @NonNull
  public List<Score> parseScores(@NonNull String raw, int gameId) {
    List<Score> scores = new ArrayList<>();
    try {
      LOG.debug("Parsing Highscore text: " + raw);
      String[] lines = raw.split("\\n");
      if (lines.length == 2) {
        return scores;
      }

      int index = 1;
      for (String line : lines) {
        if (line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#")) {
          Score score = createScore(line, gameId);
          if(score != null) {
            scores.add(score);
          }

          index++;
        }

        if (scores.size() == 3) {
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to parse highscore: " + e.getMessage() + "\nRaw Data:\n==================================\n" + raw, e);
    }
    return scores;
  }

  @Nullable
  private Score createScore(@NonNull String line, int gameId) {
    List<String> collect = Arrays.stream(line.trim().split(" ")).filter(s -> s.trim().length() > 0).collect(Collectors.toList());
    String indexString = collect.get(0).replaceAll("[^0-9]", "");

    Player p = null;
    int index = Integer.parseInt(indexString);

    if (collect.size() == 2) {
      return null;
    }
    else if (collect.size() == 3) {
      String score = collect.get(2);
      String initials = collect.get(1);
      Optional<Player> player = playerService.getPlayerForInitials(initials);
      if (player.isPresent()) {
        p = player.get();
      }
      return new Score(gameId, initials, p, score, toNumericScore(score), index);
    }
    else if (collect.size() > 3) {
      StringBuilder initials = new StringBuilder();
      for (int i = 1; i < collect.size() - 1; i++) {
        initials.append(collect.get(i));
        initials.append(" ");
      }
      String score = collect.get(collect.size() - 1);
      String playerInitials = initials.toString().trim();
      Optional<Player> player = playerService.getPlayerForInitials(playerInitials);
      if (player.isPresent()) {
        p = player.get();
      }
      return new Score(gameId, playerInitials, p, score, toNumericScore(score), index);
    }
    else {
      throw new UnsupportedOperationException("Could parse score line for game " + gameId + " '" + line + "'");
    }
  }

  private static double toNumericScore(String score) {
    String cleanScore = score.trim().replaceAll("\\.", "").replaceAll(",", "");
    return Double.parseDouble(cleanScore);
  }
}
