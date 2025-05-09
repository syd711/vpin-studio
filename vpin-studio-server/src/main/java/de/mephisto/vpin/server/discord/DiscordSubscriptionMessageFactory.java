package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.mephisto.vpin.server.discord.DiscordChannelMessageFactory.HIGHSCORE_INDICATOR;
import static de.mephisto.vpin.server.discord.DiscordChannelMessageFactory.JOIN_INDICATOR;

@Service
public class DiscordSubscriptionMessageFactory {

  private static final String DISCORD_COMPETITION_CREATED_TEMPLATE = "%s started a new subscription!\n(ID: %s)";

  private static final String COMPETITION_JOINED_TEMPLATE = "%s has " + JOIN_INDICATOR + " the subscription channel.";

  @Autowired
  private PlayerService playerService;

  public String createSubscriptionCreatedMessage(long serverId, long initiatorId, String uuid) {
    String userId = "<@" + initiatorId + ">";

    return String.format(DISCORD_COMPETITION_CREATED_TEMPLATE, userId, uuid);
  }

  public String createFirstSubscriptionHighscoreMessage(@NonNull Game game,
                                                        @NonNull Competition competition,
                                                        @NonNull Score newScore,
                                                        int scoreLimit) {
    playerService.validateInitials(newScore);
    String template = "Here is the " + HIGHSCORE_INDICATOR + "\n(ID: %s)\n";
    String msg = String.format(template, competition.getUuid());
    return msg + DiscordChannelMessageFactory.createInitialHighscoreList(newScore, scoreLimit);
  }

  public String createSubscriptionHighscoreCreatedMessage(@NonNull Game game,
                                                          @NonNull Competition competition,
                                                          @NonNull Score oldScore,
                                                          @NonNull Score newScore,
                                                          List<Score> updatedScores) {
    playerService.validateInitials(newScore);
    String playerName = resolvePlayerName(competition.getDiscordServerId(), newScore);
    String template = "**%s created a new highscore!**\n(ID: %s)\n" +
        "```%s\n" +
        "```";
    String msg = String.format(template, playerName, competition.getUuid(), newScore);
    msg = msg + getBeatenMessage(competition.getDiscordServerId(), oldScore, newScore);

    return msg + "\nHere is the " + HIGHSCORE_INDICATOR + ":" + DiscordChannelMessageFactory.createHighscoreList(updatedScores, competition.getScoreLimit());
  }

  public String createSubscriptionJoinedMessage(@NonNull Competition competition, @NonNull DiscordMember bot) {
    String playerName = "<@" + bot.getId() + ">";
    return String.format(COMPETITION_JOINED_TEMPLATE, playerName, competition.getUuid());
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
      return "The previous highscore of " + oldScore.getFormattedScore() + " has been beaten.";
    }

    if (newScore.getPlayerInitials().equals(oldScore.getPlayerInitials())) {
      return "The player has beaten their own highscore.";
    }

    String beatenMessageTemplate = "%s, your highscore of %s points has been beaten.";
    return String.format(beatenMessageTemplate, oldName, oldScore.getFormattedScore());
  }
}
