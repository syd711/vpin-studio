package de.mephisto.vpin.restclient.client;


import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Discord
 ********************************************************************************************************************/
public class DiscordServiceClient extends VPinStudioClientService {

  DiscordServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "discord/clearcache", Boolean.class);
  }

  public DiscordCompetitionData getDiscordCompetitionData(long serverId, long channelId) {
    return getRestClient().get(API + "discord/competition/" + serverId + "/" + channelId, DiscordCompetitionData.class);
  }

  public DiscordBotStatus getDiscordStatus(long serverId) {
    return getRestClient().get(API + "discord/status/" + serverId, DiscordBotStatus.class);
  }

  public List<DiscordChannel> getDiscordChannels() {
    return Arrays.asList(getRestClient().getCached(API + "discord/channels", DiscordChannel[].class));
  }

  public PlayerRepresentation getDiscordPlayer(long serverId, long memberId) {
    return getRestClient().getCached(API + "discord/player/" + serverId + "/" + memberId, PlayerRepresentation.class);
  }

  public List<DiscordChannel> getDiscordChannels(long serverId) {
    return Arrays.asList(getRestClient().getCached(API + "discord/channels/" + serverId, DiscordChannel[].class));
  }

  public DiscordServer getDiscordServer(long serverId) {
    return getRestClient().getCached(API + "discord/server/" + serverId, DiscordServer.class);
  }

  public List<DiscordServer> getDiscordServers() {
    return Arrays.asList(getRestClient().getCached(API + "discord/servers", DiscordServer[].class));
  }

  public boolean isCompetitionActive(long discordServerId, long discordChannelId, String uuid) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "discord/competition/isactive/" + discordServerId + "/" + discordChannelId + "/" + uuid, Boolean.class);
  }

  public List<PlayerRepresentation> getDiscordUsers(long serverId) {
    return Arrays.asList(getRestClient().get(API + "discord/users/" + serverId, PlayerRepresentation[].class));
  }

  public List<PlayerRepresentation> getAllowList() {
    return Arrays.asList(getRestClient().get(API + "discord/allowlist", PlayerRepresentation[].class));
  }
}
