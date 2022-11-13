package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class HighscoreParser {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreParser.class);

  public static List<Score> parseScores(Game game, String raw) {
    List<Score> scores = new ArrayList<>();
    try {
      LOG.debug("Parsing Highscore text for " + game.getGameDisplayName() + "\n" + raw);
      String[] lines = raw.split("\\n");
      if (lines.length == 2) {
        Score score = new Score(null, lines[1].trim(), 1);
        scores.add(score);
        return scores;
      }

      int index = 1;
      for (String line : lines) {
        if (line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#")) {
          Score score = createScore(line);
          scores.add(score);
          index++;
        }

        if (scores.size() == 3) {
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to parse highscore for '" + game + "': " + e.getMessage() + "\nRaw Data:\n==================================\n" + raw, e);
    }
    return scores;
  }

  private void parseTwoLineOutput(Highscore highscore, String line) {

  }

  private static Score createScore(String line) {
    List<String> collect = Arrays.stream(line.trim().split(" ")).filter(s -> s.trim().length() > 0).collect(Collectors.toList());
    String indexString = collect.get(0).replaceAll("[^0-9]", "");
    int index = Integer.parseInt(indexString);
    if (collect.size() == 2) {
      return new Score(null, collect.get(1), index);
    }
    else if (collect.size() == 3) {
      return new Score(collect.get(1), collect.get(2), index);
    }
    else if (collect.size() > 3) {
      StringBuilder initials = new StringBuilder();
      for (int i = 1; i < collect.size() - 1; i++) {
        initials.append(collect.get(i));
        initials.append(" ");
      }
      return new Score(initials.toString().trim(), collect.get(collect.size() - 1), index);
    }
    else {
      throw new UnsupportedOperationException("Could parse score line '" + line + "'");
    }
  }

  public static String formatScore(String score) {
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    decimalFormat.setGroupingUsed(true);
    decimalFormat.setGroupingSize(3);
    return decimalFormat.format(Long.parseLong(score));
  }
}
