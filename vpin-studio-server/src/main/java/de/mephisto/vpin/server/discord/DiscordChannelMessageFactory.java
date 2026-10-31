package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.highscores.Score;

import java.util.List;

/**
 * Helper for formatting highscore lists for Discord messages.
 */
public class DiscordChannelMessageFactory {
  public static final String FINISHED_INDICATOR = " finished";

  public static String createHighscoreList(List<Score> scores, int scoreLimit) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos   Initials           Score\n");
    builder.append("------------------------------\n");
    int count = 0;
    for (Score score : scores) {
      builder.append("#");
      builder.append(score.getPosition());
      if (String.valueOf(score.getPosition()).length() == 1) {
        builder.append(" ");
      }
      builder.append("  ");
      builder.append(String.format("%4.4s", score.getPlayerInitials()));
      builder.append("    ");
      builder.append(String.format("%17.17s", score.getFormattedScore()));
      builder.append("\n");

      count++;
    }

    while (count < scoreLimit) {
      builder.append("#");
      builder.append(count + 1);
      if (String.valueOf(count + 1).length() == 1) {
        builder.append(" ");
      }
      builder.append("  ");
      builder.append(String.format("%4.4s", "???"));
      builder.append("    ");
      builder.append(String.format("%17.17s", 0));
      builder.append("\n");
      count++;
    }

    builder.append("```");

    return builder.toString();
  }

}
