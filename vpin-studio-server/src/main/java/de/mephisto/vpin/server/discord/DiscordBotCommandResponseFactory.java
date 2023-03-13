package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DiscordBotCommandResponseFactory {

  public static final String COMMAND_SUMMARY = "List of available commands:\n" +
      "**/competitions **: Returns the list and status of active competitions.\n" +
      "**/find <TERM>**: Returns the list of tables matching the given search term, including their id.\n" +
      "**/hs <TABLE NAME | ID>**: Returns the highscore for the table matching the give name or id.\n" +
      "**/ranks **: Returns the overall player ranking.\n" +
      "**/recent **: Returns the latest highscores.\n" +
      "**/player <PLAYER_INITIALS> **: Returns all data of this player.\n" +
      "";

  private static final String COMPETITION_ACTIVE_TEMPLATE = "" +
      "```\n" +
      "%s\n" +
      "------------------------------------------------------\n" +
      "Type:        %s\n" +
      "Table:       %s\n" +
      "Start Date:  %s\n" +
      "End Date:    %s\n" +
      "Duration:    %s days\n" +
      "\n";


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

    String cType = "offline";
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      cType = "Discord";
    }
    String format = String.format(COMPETITION_ACTIVE_TEMPLATE, competition.getName(),
        cType,
        game.getGameDisplayName(),
        DateFormat.getDateInstance().format(competition.getStartDate()),
        DateFormat.getDateInstance().format(competition.getEndDate()),
        diff);


    StringBuilder msgBuilder = new StringBuilder(format);
    List<Score> scores = summary.getScores();
    for (Score score : scores) {
      msgBuilder.append(formatScoreEntry(score));
      msgBuilder.append("\n");
    }

    if (scores.isEmpty()) {
      msgBuilder.append("No score has been record yet.\n");
    }
    msgBuilder.append("------------------------------------------------------```");
    return msgBuilder.toString();
  }


  private static String formatScoreEntry(Score score) {
    StringBuilder builder = new StringBuilder("#");
    builder.append(score.getPosition());
    builder.append(" ");

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


  public static String createRanksMessage(List<RankedPlayer> playersByRanks) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos  Points  Initials      #1   #2   #3   Competitions\n");
    builder.append("------------------------------------------------------\n");
    int index = 0;
    for (RankedPlayer playersByRank : playersByRanks) {
      index++;
      builder.append("#");
      builder.append(index);
      builder.append("     ");
      builder.append(String.format("%4.4s", playersByRank.getPoints()));
      builder.append("  ");
      builder.append(playersByRank.getInitials());
      builder.append("          ");
      builder.append(String.format("%3.3s", playersByRank.getFirst()));
      builder.append("  ");
      builder.append(String.format("%3.3s", playersByRank.getSecond()));
      builder.append("  ");
      builder.append(String.format("%3.3s", playersByRank.getThird()));
      builder.append("       ");
      builder.append(String.format("%3.3s", playersByRank.getCompetitionsWon()));
      builder.append("\n");
    }
    builder.append("```");

    return builder.toString();
  }

  public static String createRanksMessageFor(GameService gameService, Player player, ScoreSummary highscores, List<Competition> offlineCompetitions, List<Competition> onlineCompetitions) {
    StringBuilder builder = new StringBuilder();

    if (highscores.getScores().isEmpty()) {
      builder.append("No highscores for player '");
      if (player.getName() != null) {
        builder.append(player.getName());
      }
      else {
        builder.append(player.getInitials());
      }
      builder.append("' have been found.");
      return builder.toString();
    }


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
      String table = gameService.getGame(score.getGameId()).getGameDisplayName();
      if (table.length() > 30) {
        table = table.substring(0, 25) + "...  ";
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

    String offline = offlineCompetitions.size() == 1 ? "competition" : "competitions";
    String online = onlineCompetitions.size() == 1 ? "competition" : "competitions";
    builder.append("\nThe player has won ");
    builder.append(offlineCompetitions.size());
    builder.append(" offline ");
    builder.append(offline);
    builder.append(" and ");
    builder.append(onlineCompetitions.size());
    builder.append(" online ");
    builder.append(online);
    builder.append(".\n");

    builder.append("```");

    return builder.toString();
  }

  public static String createRecentHighscoresMesssage(GameService gameService, ScoreSummary recentHighscores) {
    List<Score> scores = recentHighscores.getScores();

    if (scores.isEmpty()) {
      return "No scores recorded yet.";
    }

    StringBuilder builder = new StringBuilder();
    for (Score score : scores) {
      Game game = gameService.getGame(score.getGameId());
      if(game == null) {
        continue;
      }

      builder.append(SimpleDateFormat.getDateTimeInstance().format(score.getCreatedAt()));
      builder.append("\t");
      builder.append(game.getGameDisplayName());
      builder.append("   ");
      builder.append(score);
      builder.append("\n");
    }
    return builder.toString();
  }
}
