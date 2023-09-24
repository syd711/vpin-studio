package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.CompetitionValidationCode;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CampaignValidationService {

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameService gameService;

  public Competition validate(@NonNull Competition competition) {
    ValidationState validationState = new ValidationState();
    competition.setValidationState(validationState);
    if (competition.getType() != null && competition.getType().equals(CompetitionType.DISCORD.name()) || competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      DiscordServer server = discordService.getServer(competition.getDiscordServerId());
      if (server == null) {
        validationState.setCode(CompetitionValidationCode.DISCORD_SERVER_NOT_FOUND);
        return competition;
      }

      DiscordChannel channel = discordService.getChannel(competition.getDiscordServerId(), competition.getDiscordChannelId());
      if (channel == null) {
        validationState.setCode(CompetitionValidationCode.DISCORD_CHANNEL_NOT_FOUND);
        return competition;
      }
    }

    Game game = gameService.getGame(competition.getGameId());
    if(game == null) {
      validationState.setCode(CompetitionValidationCode.GAME_NOT_FOUND);
    }

    return competition;
  }
}
