package de.mephisto.vpin.restclient.discord;


import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.SubscriptionInfo;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Discord
 ********************************************************************************************************************/
public class DiscordServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public DiscordServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    client.clearDiscordCache();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "discord/clearcache", Boolean.class);
  }

  public DiscordCompetitionData getDiscordCompetitionData(long serverId, long channelId) {
    return getRestClient().get(API + "discord/competition/" + serverId + "/" + channelId, DiscordCompetitionData.class);
  }

  public DiscordBotStatus validateSettings() {
    return getRestClient().get(API + "discord/validate", DiscordBotStatus.class);
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

  public List<DiscordServer> getAdministratedDiscordServers() {
    return Arrays.asList(getRestClient().getCached(API + "discord/myservers", DiscordServer[].class));
  }

  public boolean isCompetitionActive(long discordServerId, long discordChannelId, String uuid) {
    final RestTemplate restTemplate = new RestTemplate();
    String url = getRestClient().getBaseUrl() + API + "discord/competition/isactive/" + discordServerId + "/" + discordChannelId + "/" + uuid;
    LOG.info("HTTP GET " + url);
    return restTemplate.getForObject(url, Boolean.class);
  }

  public SubscriptionInfo getSubscriptionInfo(long discordServerId, long discordChannelId) {
    return getRestClient().getCached(API + "discord/subscription/info/" + discordServerId + "/" + discordChannelId, SubscriptionInfo.class);
  }

  public List<PlayerRepresentation> getDiscordUsers(long serverId) {
    return Arrays.asList(getRestClient().get(API + "discord/users/" + serverId, PlayerRepresentation[].class));
  }

  public List<PlayerRepresentation> getAllowList() {
    return Arrays.asList(getRestClient().get(API + "discord/allowlist", PlayerRepresentation[].class));
  }

  public boolean checkCompetition(CompetitionRepresentation selectedItem) {
    return getRestClient().get(API + "discord/competition/check/" + selectedItem.getId(), Boolean.class);
  }
}
