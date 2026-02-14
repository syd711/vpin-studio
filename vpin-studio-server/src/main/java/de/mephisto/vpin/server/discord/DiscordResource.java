package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.competitions.SubscriptionInfo;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.server.players.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "discord")
public class DiscordResource {

  @Autowired
  private DiscordService discordService;

  @Autowired
  private DiscordCompetitionService discordCompetitionService;

  @GetMapping("/status/{serverId}")
  public DiscordBotStatus getStatus(@PathVariable("serverId") long serverId) {
    return discordService.getStatus(serverId);
  }
  @GetMapping("/validate")
  public DiscordBotStatus validateSettings() {
    return discordService.validateSettings();
  }

  @GetMapping("/permissions/competitions/join/{serverId}/{channelId}")
  public boolean hasJoinPermissions(@PathVariable("serverId") long serverId, @PathVariable("channelId") long channelId) {
    return discordService.hasJoinPermissions(serverId, channelId, discordService.getBotId());
  }

  @GetMapping("/permissions/competitions/managechannel/{serverId}/{channelId}")
  public boolean hasManagePermissions(@PathVariable("serverId") long serverId, @PathVariable("channelId") long channelId) {
    return discordService.hasManageChannelPermissions(serverId, channelId, discordService.getBotId());
  }

  @GetMapping("/permissions/competitions/managechannel/{serverId}")
  public boolean hasManagePermissions(@PathVariable("serverId") long serverId) {
    return discordService.hasManageChannelPermissions(serverId, discordService.getBotId());
  }

  /**
   * Used for the competition joining, find the latest competition for the given channel
   *
   * @return
   */
  @GetMapping("/competition/{serverId}/{channelId}")
  public DiscordCompetitionData getCompetitionData(@PathVariable("serverId") long serverId, @PathVariable("channelId") long channelId) {
    return discordService.getCompetitionData(serverId, channelId);
  }

  @GetMapping("/channels")
  public List<DiscordChannel> getChannels() {
    return discordService.getChannels();
  }

  @GetMapping("/channels/{serverId}")
  public List<DiscordChannel> getChannels(@PathVariable("serverId") long serverId) {
    return discordService.getChannels(serverId);
  }

  @GetMapping("/player/{serverId}/{id}")
  public Player getPlayer(@PathVariable("serverId") long serverId, @PathVariable("id") long id) {
    return discordService.getPlayer(serverId, id);
  }

  @GetMapping("/server/{serverId}")
  public DiscordServer getServer(@PathVariable("serverId") long serverId) {
    return discordService.getServer(serverId);
  }


  @GetMapping("/subscription/info/{serverId}/{channelId}")
  public SubscriptionInfo getSubscriptionInfo(@PathVariable("serverId") long serverId,
                                              @PathVariable("channelId") long channelId) {
    return discordService.getSubscriptionInfo(serverId, channelId);
  }

  @GetMapping("/competition/check/{id}")
  public boolean checkCompetitionScores(@PathVariable("id") int id) {
    return discordCompetitionService.runCompetitionCheck(id);
  }

  @GetMapping("/users/{serverId}")
  public List<Player> getPlayers(@PathVariable("serverId") long serverId) {
    return discordService.getPlayers(serverId);
  }

  @GetMapping("/allowlist")
  public List<Player> getAllowList() {
    return discordService.getAllowList();
  }

  @GetMapping("/servers")
  public List<DiscordServer> getServers() {
    return discordService.getServers();
  }

  @GetMapping("/myservers")
  public List<DiscordServer> getAdministratedServers() {
    return discordService.getMyServers();
  }

  @GetMapping("/competition/isactive/{serverId}/{channelId}/{uuid}")
  public boolean isCompetitionActive(@PathVariable("serverId") long serverId,
                                     @PathVariable("channelId") long channelId,
                                     @PathVariable("uuid") String uuid) {
    return discordService.isCompetitionActive(serverId, channelId, uuid);
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return discordService.clearCache();
  }
}
