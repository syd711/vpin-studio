package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscordChannelMessageFactory {
  public static final String START_INDICATOR = "started a new";
  public static final String CANCEL_INDICATOR = " cancelled";
  public static final String JOIN_INDICATOR = " joined";
  public static final String FINISHED_INDICATOR = " finished";
  public static final String HIGHSCORE_INDICATOR = "updated highscore list";

  private static final String DISCORD_COMPETITION_CREATED_TEMPLATE = "%s " + START_INDICATOR + " competition!\n(ID: %s)";


  private static final String COMPETITION_CANCELLED_TEMPLATE = "%s has " + CANCEL_INDICATOR + " the competition \"%s\".";
  private static final String COMPETITION_CANCELLED_ANONYMOUS_TEMPLATE = "The competition \"%s\" has been " + CANCEL_INDICATOR + ".";
  private static final String COMPETITION_JOINED_TEMPLATE = "%s has " + JOIN_INDICATOR + " the competition \"%s\".\n(ID: %s)";
  private static final String COMPETITION_FINISHED_INCOMPLETE = "The competition \"%s\" has been " + DiscordChannelMessageFactory.FINISHED_INDICATOR + ", " +
    "but no winner could be determined:\n" +
    "No scores have been found.\n(ID: %s)";
  private static final String COMPETITION_FINISHED_TEMPLATE =
    "The competition \"%s\" has been finished!\n(ID: %s)";

  @Autowired
  private PlayerService playerService;

  public String createDiscordCompetitionCreatedMessage(long serverId, long initiatorId, String uuid) {
    String userId = "<@" + initiatorId + ">";

    return String.format(DISCORD_COMPETITION_CREATED_TEMPLATE, userId, uuid);
  }

  public String createFirstCompetitionHighscoreCreatedMessage(@NonNull Game game,
                                                              @NonNull Competition competition,
                                                              @NonNull Score newScore,
                                                              int scoreCount) {
    playerService.validateInitials(newScore);
    String playerName = resolvePlayerName(competition.getDiscordServerId(), newScore);
    String template = "**%s created the first highscore for the \"%s\" competition.**\n(ID: %s)\n" +
      "```%s\n" +
      "```";

    //do not use the original Score#toString() method as the online position does not match with the persisted score
    String score = "#1 " + newScore.getPlayerInitials() + "   " + newScore.getScore();
    String msg = String.format(template, playerName, competition.getName(), competition.getUuid(), score);
    return msg + "\nHere is the " + HIGHSCORE_INDICATOR + ":" + createInitialHighscoreList(newScore, scoreCount);

  }

  public String createCompetitionHighscoreCreatedMessage(@NonNull Game game,
                                                         @NonNull Competition competition,
                                                         @NonNull Score oldScore,
                                                         @NonNull Score newScore,
                                                         List<Score> updatedScores) {
    playerService.validateInitials(newScore);
    String playerName = resolvePlayerName(competition.getDiscordServerId(), newScore);
    String template = "**%s created a new highscore for the \"%s\" competition.**\n(ID: %s)\n" +
      "```%s\n" +
      "```";
    String msg = String.format(template, playerName, game.getGameDisplayName(), competition.getUuid(), newScore);
    msg = msg + getBeatenMessage(competition.getDiscordServerId(), oldScore, newScore);

    return msg + "\nHere is the " + HIGHSCORE_INDICATOR + ":" + createHighscoreList(updatedScores, competition.getScoreLimit());
  }

  public String createCompetitionFinishedMessage(@NonNull Competition competition, ScoreSummary summary) {
    if (summary.getScores().isEmpty()) {
      return String.format(COMPETITION_FINISHED_INCOMPLETE, competition.getName(), competition.getUuid());
    }

    return String.format(COMPETITION_FINISHED_TEMPLATE, competition.getName(), competition.getUuid());
  }

  public String createCompetitionCancelledMessage(Player player, Competition competition) {
    if (player != null) {
      String playerName = "<@" + player.getId() + ">";
      return String.format(COMPETITION_CANCELLED_TEMPLATE, playerName, competition.getName());
    }
    return String.format(COMPETITION_CANCELLED_ANONYMOUS_TEMPLATE, competition.getName());
  }

  public String createCompetitionJoinedMessage(@NonNull Competition competition, @NonNull DiscordMember bot) {
    String playerName = "<@" + bot.getId() + ">";
    return String.format(COMPETITION_JOINED_TEMPLATE, playerName, competition.getName(), competition.getUuid());
  }

  private String resolvePlayerName(long serverId, Score newScore) {
    String playerName = newScore.getPlayerInitials();
    if (newScore.getPlayer() != null) {
      Player player = playerService.getPlayerForInitials(serverId, newScore.getPlayerInitials());
      if (player != null) {
        playerName = newScore.getPlayer().getName();
        if (PlayerDomain.DISCORD.name().equals(player.getDomain())) {
          playerName = "<@" + player.getId() + ">";
        }
      }
    }
    return playerName;
  }


  private String getBeatenMessage(long serverId, Score oldScore, Score newScore) {
    String oldName = resolvePlayerName(serverId, oldScore);
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

  public static String createHighscoreList(List<Score> scores, int scoreLimit) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos   Initials           Score\n");
    builder.append("------------------------------\n");
    int count = 0;
    for (Score score : scores) {
      builder.append("#");
      builder.append(score.getPosition());
      if (String.valueOf(score.getPosition()).length() == 1) {
        builder.append(" ");
      }
      builder.append("  ");
      builder.append(String.format("%4.4s", score.getPlayerInitials()));
      builder.append("    ");
      builder.append(String.format("%17.17s", score.getFormattedScore()));
      builder.append("\n");

      count++;
    }

    while(count < scoreLimit) {
      builder.append("#");
      builder.append(count+1);
      if (String.valueOf(count+1).length() == 1) {
        builder.append(" ");
      }
      builder.append("  ");
      builder.append(String.format("%4.4s", "???"));
      builder.append("    ");
      builder.append(String.format("%17.17s", 0));
      builder.append("\n");
      count++;
    }

    builder.append("```");

    return builder.toString();
  }

  public static String createInitialHighscoreList(Score score, int length) {
    StringBuilder builder = new StringBuilder();
    builder.append("```");
    builder.append("Pos   Initials           Score\n");
    builder.append("------------------------------\n");
    builder.append("#1");
    builder.append("   ");
    builder.append(String.format("%4.4s", score.getPlayerInitials()));
    builder.append("    ");
    builder.append(String.format("%17.17s", score.getScore()));
    builder.append("\n");

    for (int i = 0; i < length - 1; i++) {
      int pos = i + 2;
      builder.append("#");
      builder.append(pos);
      if (pos < 10) {
        builder.append(" ");
      }
      builder.append("  ");
      builder.append(String.format("%4.4s", "???"));
      builder.append("    ");
      builder.append(String.format("%17.17s", "0"));
      builder.append("\n");
    }
    builder.append("```");

    return builder.toString();
  }
}
