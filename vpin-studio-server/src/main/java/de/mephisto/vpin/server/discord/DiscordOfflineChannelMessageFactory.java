package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

public class DiscordOfflineChannelMessageFactory {
  private static final String COMPETITION_FINISHED_TEMPLATE =
      "The competition \"%s\" has been finished!\n";

  private static final String COMPETITION_FINISHED_INCOMPLETE = "The competition \"%s\" has been " + DiscordChannelMessageFactory.FINISHED_INDICATOR + ", " +
      "but no winner could be determined:\n" +
      "No scores have been found.";

  private static final String COMPETITION_CANCELLED_TEMPLATE = "The competition \"%s\" has been cancelled.";

  public static String createCompetitionCancelledMessage(Competition competition) {
    return String.format(COMPETITION_CANCELLED_TEMPLATE, competition.getName());
  }


  public static String createHighscoreCreatedMessage(@NonNull HighscoreParsingService highscoreParsingService, @NonNull HighscoreChangeEvent event, @Nullable String raw) {
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
      List<Score> scores = highscoreParsingService.parseScores(new Date(), raw, game, -1);
      String highscoreList = DiscordChannelMessageFactory.createHighscoreList(scores, -1);
      msg = msg + "\nHere is the current highscore:\n" + highscoreList;
    }

    return msg;
  }

  public static String createCompetitionHighscoreCreatedMessage(@NonNull HighscoreParsingService highscoreParsingService, @NonNull Competition competition, @NonNull HighscoreChangeEvent event, @Nullable String raw) {
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
      List<Score> scores = highscoreParsingService.parseScores(new Date(), raw, game, -1);
      String highscoreList = DiscordChannelMessageFactory.createHighscoreList(scores, -1);
      msg = msg + "\nHere is the current highscore:\n" + highscoreList;
    }

    return msg;
  }


  public static String createOfflineCompetitionCreatedMessage(Competition competition, Game game) {
    return "A new competition has been started!\n";
  }

  public static String createCompetitionFinishedMessage(@NonNull Competition competition, @Nullable Player winner, Game game, ScoreSummary summary) {
    if (summary.getScores().isEmpty()) {
      return String.format(COMPETITION_FINISHED_INCOMPLETE, competition.getName());
    }

    String competitionName = competition.getName();
    return String.format(COMPETITION_FINISHED_TEMPLATE, competitionName);
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

    if (oldScore.isSkipped()) {
      return "";
    }

    if (StringUtils.isEmpty(oldName)) {
      return "The previous highscore of " + oldScore.getScore() + " has been beaten.";
    }

    if (newScore.getPlayerInitials().equals(oldScore.getPlayerInitials())) {
      return "The player has beaten their own highscore.";
    }

    String beatenMessageTemplate = "%s, your highscore of %s points has been beaten.";
    return String.format(beatenMessageTemplate, oldName, oldScore.getFormattedScore());
  }
}
