package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.Score;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class ScoreHelper {
  private final static Logger LOG = LoggerFactory.getLogger(ScoreHelper.class);


  public static String formatScoreEntry(ScoreSummary summary, int index) {
    StringBuilder builder = new StringBuilder("#");
    builder.append((index + 1));
    builder.append(" ");

    if (summary.getScores().size() > index) {
      Score score = summary.getScores().get(index);
      String playerName = score.getPlayerInitials();
      if (score.getPlayer() != null) {
        playerName = score.getPlayer().getName();
      }
      builder.append(playerName);
      while (builder.toString().length() < 30) {
        builder.append(" ");
      }
      builder.append("   ");
      builder.append(score.getScore());
    }

    return builder.toString();
  }
}
