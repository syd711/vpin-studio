package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DiscordChannelMessageFactory {
  private static final String DISCORD_COMPETITION_CREATED_TEMPLATE = "%s started a new competition!\n" +
      "```\n" +
      "%s\n" +
      "(ID: %s)\n" +
      "------------------------------------------------------------\n" +
      "Table:       %s\n" +
      "Start Date:  %s\n" +
      "End Date:    %s\n" +
      "Duration:    %s days\n" +
      "------------------------------------------------------------```";

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

    String template = "%s created a new highscore for '%s', competed in '%s' (ID: %s) .\n" +
        "```%s\n" +
        "```";
    String otherPlayerTemplate = "\n%s, your highscore of %s points has been beaten.";

    String msg = String.format(template, newName, competition.getName(), competition.getUuid(), game.getGameDisplayName(), newScore);
    String suffix = String.format(otherPlayerTemplate, oldName, oldScore.getScore());

    String result = msg;
    if (StringUtils.isEmpty(oldName)) {
      result = result + "\nThe previous highscore of " + oldScore.getScore() + " has been beaten.";
    }
    else if (!oldName.equals(newName)) {
      result = result + suffix;
    }
    return result;
  }

  public static String createDiscordCompetitionCreatedMessage(Competition competition, Game game, long initiatorId) {
    LocalDate start = competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    long diff = Math.abs(ChronoUnit.DAYS.between(end, start));
    String userId = "<@" + initiatorId + ">";

    return String.format(DISCORD_COMPETITION_CREATED_TEMPLATE,
        userId,
        competition.getName(),
        competition.getUuid(),
        game.getGameDisplayName(),
        DateFormat.getDateInstance().format(competition.getStartDate()),
        DateFormat.getDateInstance().format(competition.getEndDate()),
        diff);
  }
}
