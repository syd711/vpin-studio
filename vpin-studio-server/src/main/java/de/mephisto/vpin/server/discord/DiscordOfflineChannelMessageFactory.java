package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.util.ScoreHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DiscordOfflineChannelMessageFactory {
  private static final String COMPETITION_CREATED_TEMPLATE = "A new competition has been started!\n" +
      "```\n" +
      "%s\n" +
      "------------------------------------------------------------\n" +
      "Table:       %s\n" +
      "Start Date:  %s\n" +
      "End Date:    %s\n" +
      "Duration:    %s days\n" +
      "------------------------------------------------------------```";

  private static final String COMPETITION_FINISHED_TEMPLATE = "Congratulation %s!\n" +
      "```" +
      "The competition '%s' has been finished!\n" +
      "And the winner is...\n" +
      "\n" +
      "        %s\n" +
      "\n" +
      "Table: %s\n" +
      "Score: %s\n" +
      "\n" +
      "%s\n" +
      "%s\n" +
      "```";

  private static final String COMPETITION_FINISHED_INCOMPLETE = "The competition '%s' has been finished, but no winner could be determined:\n" +
      "No scores have been found.";

  private static final String COMPETITION_CANCELLED_TEMPLATE = "The competition \"%s\" has been cancelled.";

  public static String createCompetitionCancelledMessage(Competition competition) {
    return String.format(COMPETITION_CANCELLED_TEMPLATE, competition.getName());
  }


  public static String createHighscoreCreatedMessage(@NonNull HighscoreChangeEvent event, @Nullable String raw) {
    Game game = event.getGame();
    Score newScore = event.getNewScore();
    Score oldScore = event.getOldScore();

    String newName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      newName = newScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        newName = "<@" + player.getId() + ">";
      }
    }

    String template = "**%s created a new highscore for \"%s\"**.\n" +
        "```%s\n" +
        "```\n";
    String msg = String.format(template, newName, game.getGameDisplayName(), newScore);
    msg = msg + getBeatenMessage(oldScore, newScore);

    if (!StringUtils.isEmpty(raw)) {
      msg = msg + "\nHere is the current highscore:\n```" + raw + "```";
    }

    return msg;
  }

  public static String createCompetitionHighscoreCreatedMessage(@NonNull Competition competition, @NonNull HighscoreChangeEvent event, @Nullable String raw) {
    Game game = event.getGame();
    Score newScore = event.getNewScore();
    Score oldScore = event.getOldScore();

    String newName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      newName = newScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        newName = "<@" + player.getId() + ">";
      }
    }

    String template = "**%s created a new highscore for \"%s\"**.\nCompetition: \"%s\"\n```%s```\n";
    String msg = String.format(template, newName, game.getGameDisplayName(), competition.getName(), newScore);
    msg = msg + getBeatenMessage(oldScore, newScore);

    if (!StringUtils.isEmpty(raw)) {
      msg = msg + "\nHere is the current highscore:\n```" + raw + "```";
    }

    return msg;
  }


  public static String createOfflineCompetitionCreatedMessage(Competition competition, Game game) {
    LocalDate start = competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    long diff = Math.abs(ChronoUnit.DAYS.between(end, start)) + 1;

    return String.format(COMPETITION_CREATED_TEMPLATE,
        competition.getName(),
        game.getGameDisplayName(),
        DateFormat.getDateInstance().format(competition.getStartDate()),
        DateFormat.getDateInstance().format(competition.getEndDate()),
        diff);
  }

  public static String createCompetitionFinishedMessage(@NonNull Competition competition, @Nullable Player winner, Game game, ScoreSummary summary) {
    if (summary.getScores().isEmpty()) {
      return String.format(COMPETITION_FINISHED_INCOMPLETE, competition.getName());
    }

    String winnerName = competition.getWinnerInitials();
    String winnerRaw = competition.getWinnerInitials();
    if (winner != null) {
      winnerName = winner.getName();
      winnerRaw = winner.getName();
      if (PlayerDomain.DISCORD.name().equals(winner.getDomain())) {
        winnerName = "<@" + winner.getId() + ">";
      }
    }

    String second = ScoreHelper.formatScoreEntry(summary, 1);
    String third = ScoreHelper.formatScoreEntry(summary, 2);

    String competitionName = competition.getName();
    if(competition.getType().equals(CompetitionType.DISCORD.name())) {
      competitionName = competitionName + " (" + competition.getUuid() + ")";
    }

    return String.format(COMPETITION_FINISHED_TEMPLATE,
        winnerName,
        competitionName,
        winnerRaw,
        game.getGameDisplayName(),
        summary.getScores().get(0).getScore(),
        second,
        third);
  }

  public static String getBeatenMessage(Score oldScore, Score newScore) {
    String oldName = oldScore.getPlayerInitials();
    if (oldScore.getPlayer() != null) {
      Player player = oldScore.getPlayer();
      oldName = oldScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        oldName = "<@" + player.getId() + ">";
      }
    }

    if (oldScore.getPlayerInitials().equals("???") || oldScore.getNumericScore() == 0) {
      return "";
    }

    if (StringUtils.isEmpty(oldName)) {
      return "The previous highscore of " + oldScore.getScore() + " has been beaten.";
    }

    if (newScore.getPlayerInitials().equals(oldScore.getPlayerInitials())) {
      return "The player has beaten their own highscore.";
    }

    String beatenMessageTemplate = "%s, your highscore of %s points has been beaten.";
    return String.format(beatenMessageTemplate, oldName, oldScore.getScore());
  }
}
