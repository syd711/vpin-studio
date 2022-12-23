package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DiscordBotCommandResponseFactory {

  private static final String COMPETITION_ACTIVE_TEMPLATE = "" +
      "```\n" +
      "%s\n" +
      "------------------------------------------------------------\n" +
      "Table:       %s\n" +
      "Start Date:  %s\n" +
      "End Date:    %s\n" +
      "Duration:    %s days\n" +
      "\n" +
      "%s\n" +
      "%s\n" +
      "%s\n" +
      "------------------------------------------------------------```";


  public static String createHighscoreMessage(Game game, ScoreSummary scoreSummary) {
    String title = "Highscore for '" + game.getGameDisplayName() + "'";
    StringBuilder builder = new StringBuilder();
    builder.append("```\n");
    builder.append(title);
    builder.append("\n");

    String line = "-";
    while (line.length() < title.length()) {
      line += "-";
    }
    builder.append(line);
    builder.append("\n");
    builder.append(scoreSummary.getRaw());
    builder.append("\n```");
    return builder.toString();
  }

  public static String createActiveCompetitionMessage(Competition competition, Game game, ScoreSummary summary) {
    LocalDate start = competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    long diff = Math.abs(ChronoUnit.DAYS.between(end, start));

    String first = formatScoreEntry(summary, 0);
    String second = formatScoreEntry(summary, 1);
    String third = formatScoreEntry(summary, 2);

    return String.format(COMPETITION_ACTIVE_TEMPLATE, competition.getName(),
        game.getGameDisplayName(),
        DateFormat.getDateInstance().format(competition.getStartDate()),
        DateFormat.getDateInstance().format(competition.getEndDate()),
        diff, first, second, third);
  }


  private static String formatScoreEntry(ScoreSummary summary, int index) {
    StringBuilder builder = new StringBuilder("#");
    builder.append(String.valueOf(index + 1));
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


  public static String createRanksMessage(List<RankedPlayer> playersByRanks) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos  Initials      #1   #2   #3\n");
    builder.append("--------------------------------\n");
    int index = 0;
    for (RankedPlayer playersByRank : playersByRanks) {
      index++;
      builder.append("#");
      builder.append(index);
      builder.append("   ");
      builder.append(playersByRank.getInitials());
      builder.append("            ");
      builder.append(playersByRank.getFirst());
      builder.append("    ");
      builder.append(playersByRank.getSecond());
      builder.append("    ");
      builder.append(playersByRank.getThird());
      builder.append("\n");
    }
    builder.append("```");

    return builder.toString();
  }

  public static String createRanksMessageFor(Player player, ScoreSummary highscores) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Highscores of '");
    if (player.getName() != null) {
      builder.append(player.getName());
    }
    else {
      builder.append(player.getInitials());
    }
    builder.append("'\n");
    builder.append("---------------------------------------------------\n");
    for (Score score : highscores.getScores()) {
      String table = score.getDisplayName();
      if (table.length() > 30) {
        table = table.substring(0, 25) + "...";
      }
      else {
        while (table.length() < 30) {
          table += " ";
        }
      }

      builder.append("#");
      builder.append(score.getPosition());
      builder.append("  ");
      builder.append(table);
      builder.append("  ");
      builder.append(score.getScore());
      builder.append("\n");
    }
    builder.append("```");

    return builder.toString();
  }
}
