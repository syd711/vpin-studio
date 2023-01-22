package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DiscordChannelMessageFactory {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordChannelMessageFactory.class);

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

  private static final String COMPETITION_CANCELLED_TEMPLATE = "```" +
      "The competition '%s' has been cancelled." +
      "```";

  public static String createCompetitionCancelledMessage(Competition competition) {
    return String.format(COMPETITION_CANCELLED_TEMPLATE, competition.getName());
  }

  public static String createCompetitionHighscoreCreatedMessage(Competition competition, HighscoreChangeEvent event) {
    Game game = event.getGame();
    Score newScore = event.getNewScore();
    Score oldScore = event.getOldScore();

    String newName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      newName = newScore.getPlayer().getName();
      if (player.getDomain().equals(PlayerDomain.DISCORD.name())) {
        newName = "<@" + player.getId() + ">";
      }
    }

    String oldName = oldScore.getPlayerInitials();
    if (oldScore.getPlayer() != null) {
      Player player = oldScore.getPlayer();
      oldName = oldScore.getPlayer().getName();
      if (player.getDomain().equals(PlayerDomain.DISCORD.name())) {
        oldName = "<@" + player.getId() + ">";
      }
    }

    String template = "%s created a new highscore for '%s', competed in '%s' .\n" +
        "```%s\n" +
        "```";
    String otherPlayerTemplate = "\n%s, your highscore of %s points has been beaten.";

    String msg = String.format(template, newName, competition.getName(), game.getGameDisplayName(), newScore);
    String suffix = String.format(otherPlayerTemplate, oldName, oldScore.getScore());

    String result = msg;
    if(StringUtils.isEmpty(oldName)) {
      result = result + "\nThe previous highscore of " + oldScore.getScore() + " has been beaten.";
    }
    else if (!oldName.equals(newName)) {
      result = result + suffix;
    }
    return result;
  }

  
  public static String createCompetitionCreatedMessage(Competition competition, Game game) {
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

  public static String createCompetitionFinishedMessage(@NonNull Competition competition, @Nullable Player winner, Game game, ScoreSummary summary) {
    String winnerName = competition.getWinnerInitials();
    String winnerRaw = competition.getWinnerInitials();
    if (winner != null) {
      winnerName = winner.getName();
      winnerRaw = winner.getName();
      if (winner.getDomain().equals(PlayerDomain.DISCORD.name())) {
        winnerName = "<@" + winner.getId() + ">";
      }
    }

    String second = DiscordWebhookMessageFactory.formatScoreEntry(summary, 1);
    String third = DiscordWebhookMessageFactory.formatScoreEntry(summary, 2);

    return String.format(COMPETITION_FINISHED_TEMPLATE,
        winnerName,
        competition.getName(),
        winnerRaw,
        game.getGameDisplayName(),
        summary.getScores().get(0).getScore(),
        second,
        third);
  }
}
