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

public class DiscordWebhookMessageFactory {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordWebhookMessageFactory.class);

  public static String createHighscoreCreatedMessage(HighscoreChangeEvent event) {
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

    String template = "%s created a new highscore for '%s'.\\n" +
        "```%s\\n" +
        "```";
    String otherPlayerTemplate = "\\n%s, your highscore of %s points has been beaten.";

    String msg = String.format(template, newName, game.getGameDisplayName(), newScore);
    String suffix = String.format(otherPlayerTemplate, oldName, oldScore.getScore());

    String result = msg;
    if(StringUtils.isEmpty(oldName)) {
      result = result + "\\nThe previous highscore of " + oldScore.getScore() + " has been beaten.";
    }
    else if (!oldName.equals(newName)) {
      result = result + suffix;
    }
    LOG.info("Hook message: " + result);
    return result;
  }

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
