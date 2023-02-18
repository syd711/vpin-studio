package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

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


  private static final String COMPETITION_CANCELLED_TEMPLATE = "%s has cancelled the competition \"%s\".";
  private static final String COMPETITION_CANCELLED_ANONYMOUS_TEMPLATE = "The competition \"%s\" has been cancelled.";

  public static String createCompetitionCancelledMessage(Player player, Competition competition) {
    if (player != null) {
      String playerName = "<@" + player.getId() + ">";
      return String.format(COMPETITION_CANCELLED_TEMPLATE, playerName, competition.getName());
    }
    return String.format(COMPETITION_CANCELLED_ANONYMOUS_TEMPLATE, competition.getName());
  }


  public static String createFirstCompetitionHighscoreCreatedMessage(@NonNull Game game,
                                                                     @NonNull Competition competition,
                                                                     @NonNull Score newScore,
                                                                     @NonNull List<Score> newScores) {
    String playerName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      playerName = newScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        playerName = "<@" + player.getId() + ">";
      }
    }

    String template = "**%s created the first highscore for \"%s\"**.\n(ID: %s)\n" +
        "```%s\n" +
        "```";

    String msg = String.format(template, playerName, game.getGameDisplayName(), competition.getUuid(), newScore);
    return msg + "Here is the updated highscore list:" + createInitialHighscoreList(newScore, newScores.size() - 1);

  }

  public static String createCompetitionHighscoreCreatedMessage(@NonNull Game game,
                                                                @NonNull Competition competition,
                                                                @NonNull Score oldScore,
                                                                @NonNull Score newScore,
                                                                List<Score> updatedScores) {
    String playerName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = newScore.getPlayer();
      playerName = newScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        playerName = "<@" + player.getId() + ">";
      }
    }

    String oldName = oldScore.getPlayerInitials();
    if (oldScore.getPlayer() != null) {
      Player player = oldScore.getPlayer();
      oldName = oldScore.getPlayer().getName();
      if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
        oldName = "<@" + player.getId() + ">";
      }
    }

    String template = "**%s created a new highscore for \"%s\"**.\n(ID: %s)\n" +
        "```%s\n" +
        "```";
    String otherPlayerTemplate = "\n%s, your highscore of %s points has been beaten.\n";

    String msg = String.format(template, playerName, game.getGameDisplayName(), competition.getUuid(), newScore);
    String suffix = String.format(otherPlayerTemplate, oldName, oldScore.getScore());

    String result = msg;
    if (StringUtils.isEmpty(oldName)) {
      result = result + "The previous highscore of " + oldScore.getScore() + " has been beaten.\n";
    }
    else if (!oldName.equals(playerName) && !oldName.equals("???")) {
      result = result + suffix;
    }
    else {
      result = result + "\nThe player has beaten their own highscore.\n";
    }


    return result + "Here is the updated highscore list:" + createHighscoreList(updatedScores);
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
