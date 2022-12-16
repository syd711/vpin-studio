package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class NotificationFactory {

  private static final String COMPETITION_CREATED_TEMPLATE = "A new competition has been started!\\n" +
      "```\\n" +
      "%s\\n" +
      "------------------------------------------------------------\\n" +
      "Table:       %s\\n" +
      "Start Date:  %s\\n" +
      "End Date:    %s\\n" +
      "Duration:    %s days\\n" +
      "------------------------------------------------------------```";

  private static final String COMPETITION_FINISHED_TEMPLATE = "```" +
      "The competition '%s' has been finished!\\n" +
      "And the winner is...\\n" +
      "\\n" +
      "        %s\\n" +
      "\\n" +
      "Table: %s\\n" +
      "Score: %s\\n" +
      "\\n" +
      "%s\\n" +
      "%s\\n" +
      "```";

  private static final String COMPETITION_CANCELLED_TEMPLATE = "```" +
      "The competition '%s' has been cancelled." +
      "```";

  private static final String HIGHSCORE_TEMPLATE = "```" +
      "A new highscore for '%s' has been created:\\n" +
      "%s\\n" +
      "```";

  public static String createDiscordCompetitionCancelledMessage(Competition competition) {
    return String.format(COMPETITION_CANCELLED_TEMPLATE, competition.getName());
  }

  public static String createDiscordCompetitionCreatedMessage(Competition competition, Game game) {
    LocalDate start = competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    long diff = Math.abs(ChronoUnit.DAYS.between(end, start));

    return String.format(COMPETITION_CREATED_TEMPLATE,
        competition.getName(),
        game.getGameDisplayName(),
        DateFormat.getDateInstance().format(competition.getStartDate()),
        DateFormat.getDateInstance().format(competition.getEndDate()),
        diff);
  }

  public static String createDiscordCompetitionFinishedMessage(Competition competition, Game game, ScoreSummary summary) {
    String playerName = competition.getWinnerInitials();
    if (competition.getWinner() != null) {
      playerName = competition.getWinner().getName();
    }

    String second = formatScoreEntry(summary, 1);
    String third = formatScoreEntry(summary, 2);

    return String.format(COMPETITION_FINISHED_TEMPLATE,
        competition.getName(),
        playerName,
        game.getGameDisplayName(),
        summary.getScores().get(0).getScore(),
        second,
        third);
  }

  public static String createDiscordHighscoreMessage(Game game, Score changedScore) {
    return String.format(HIGHSCORE_TEMPLATE,game.getGameDisplayName(), changedScore.toString());
  }

  private static String formatScoreEntry(ScoreSummary summary, int index) {
    StringBuilder builder = new StringBuilder("#");
    builder.append(String.valueOf(index + 1));
    builder.append(" ");

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
    return builder.toString();
  }
}
