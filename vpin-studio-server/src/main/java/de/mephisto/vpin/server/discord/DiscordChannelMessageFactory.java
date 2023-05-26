package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.util.ScoreHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

public class DiscordChannelMessageFactory {
  public static final String START_INDICATOR = "started a new competition";
  public static final String CANCEL_INDICATOR = " cancelled";
  public static final String JOIN_INDICATOR = " joined";
  public static final String FINISHED_INDICATOR = " finished";
  public static final String HIGHSCORE_INDICATOR = "updated highscore list";

  private static final String DISCORD_COMPETITION_CREATED_TEMPLATE = "%s " + START_INDICATOR + "!\n(ID: %s)";


  private static final String COMPETITION_CANCELLED_TEMPLATE = "%s has " + CANCEL_INDICATOR + " the competition \"%s\".";
  private static final String COMPETITION_CANCELLED_ANONYMOUS_TEMPLATE = "The competition \"%s\" has been " + CANCEL_INDICATOR + ".";
  private static final String COMPETITION_JOINED_TEMPLATE = "%s has " + JOIN_INDICATOR + " the competition \"%s\".\n(ID: %s)";
  private static final String COMPETITION_FINISHED_INCOMPLETE = "The competition \"%s\" has been " + DiscordChannelMessageFactory.FINISHED_INDICATOR + ", " +
      "but no winner could be determined:\n" +
      "No scores have been found.";
  private static final String COMPETITION_FINISHED_TEMPLATE = "Congratulation %s!\n" +
      "```" +
      "The competition \"%s\" has been finished!\n" +
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


  public static String createDiscordCompetitionCreatedMessage(long initiatorId, String uuid) {
    String userId = "<@" + initiatorId + ">";

    return String.format(DISCORD_COMPETITION_CREATED_TEMPLATE, userId, uuid);
  }

  public static String createFirstCompetitionHighscoreCreatedMessage(@NonNull Game game,
                                                                     @NonNull Competition competition,
                                                                     @NonNull Score newScore,
                                                                     int scoreCount) {
    String playerName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      playerName = newScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        playerName = "<@" + player.getId() + ">";
      }
    }

    String template = "**%s created the first highscore for the \"%s\" competition.**\n(ID: %s)\n" +
        "```%s\n" +
        "```";

    //do not use the original Score#toString() method as the online position does not match with the persisted score
    String score = "#1 " + newScore.getPlayerInitials() + "   " + newScore.getScore();
    String msg = String.format(template, playerName, competition.getName(), competition.getUuid(), score);
    return msg + "\nHere is the " + HIGHSCORE_INDICATOR + ":" + createInitialHighscoreList(newScore, scoreCount - 1);

  }

  public static String createCompetitionHighscoreCreatedMessage(@NonNull Game game,
                                                                @NonNull Competition competition,
                                                                @NonNull Score oldScore,
                                                                @NonNull Score newScore,
                                                                List<Score> updatedScores) {
    String newName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      newName = newScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        newName = "<@" + player.getId() + ">";
      }
    }


    String template = "**%s created a new highscore for the \"%s\" competition.**\n(ID: %s)\n" +
        "```%s\n" +
        "```";
    String msg = String.format(template, newName, game.getGameDisplayName(), competition.getUuid(), newScore);
    msg = msg + DiscordOfflineChannelMessageFactory.getBeatenMessage(oldScore, newScore);

    return msg + "\nHere is the " + HIGHSCORE_INDICATOR + ":" + createHighscoreList(updatedScores);
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
    return String.format(COMPETITION_FINISHED_TEMPLATE,
        winnerName,
        competitionName,
        winnerRaw,
        game.getGameDisplayName(),
        summary.getScores().get(0).getScore(),
        second,
        third);
  }

  public static String createCompetitionCancelledMessage(Player player, Competition competition) {
    if (player != null) {
      String playerName = "<@" + player.getId() + ">";
      return String.format(COMPETITION_CANCELLED_TEMPLATE, playerName, competition.getName());
    }
    return String.format(COMPETITION_CANCELLED_ANONYMOUS_TEMPLATE, competition.getName());
  }

  public static String createCompetitionJoinedMessage(@NonNull Competition competition, @NonNull DiscordMember bot) {
    String playerName = "<@" + bot.getId() + ">";
    return String.format(COMPETITION_JOINED_TEMPLATE, playerName, competition.getName(), competition.getUuid());
  }


  private static String createHighscoreList(List<Score> scores) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos   Initials           Score\n");
    builder.append("------------------------------\n");
    int index = 0;
    for (Score score : scores) {
      index++;
      builder.append("#");
      builder.append(score.getPosition());
      builder.append("   ");
      builder.append(String.format("%4.4s", score.getPlayerInitials()));
      builder.append("       ");
      builder.append(String.format("%14.12s", score.getScore()));
      builder.append("\n");
    }
    builder.append("```");

    return builder.toString();
  }

  private static String createInitialHighscoreList(Score score, int length) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos   Initials           Score\n");
    builder.append("------------------------------\n");
    builder.append("#1");
    builder.append("   ");
    builder.append(String.format("%4.4s", score.getPlayerInitials()));
    builder.append("       ");
    builder.append(String.format("%14.12s", score.getScore()));
    builder.append("\n");

    for (int i = 0; i < length; i++) {
      builder.append("#");
      builder.append((i + 2));
      builder.append("   ");
      builder.append(String.format("%4.4s", "???"));
      builder.append("       ");
      builder.append(String.format("%14.12s", "0"));
      builder.append("\n");
    }
    builder.append("```");

    return builder.toString();
  }
}
