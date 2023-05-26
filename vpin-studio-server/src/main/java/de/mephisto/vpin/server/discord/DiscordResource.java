package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.discord.*;
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

  @GetMapping("/status")
  public DiscordBotStatus getStatus() {
    return discordService.getStatus();
  }

  @GetMapping("/permissions/competitions/join/{serverId}/{channelId}")
  public boolean hasJoinPermissions(@PathVariable("serverId") long serverId, @PathVariable("channelId") long channelId) {
    return discordService.hasJoinPermissions(serverId, channelId, discordService.getBotId());
  }

  @GetMapping("/permissions/competitions/manage/{serverId}/{channelId}")
  public boolean hasManagePermissions(@PathVariable("serverId") long serverId, @PathVariable("channelId") long channelId) {
    return discordService.hasManagePermissions(serverId, channelId, discordService.getBotId());
  }

  /**
   * Used for the competition joining, find the latest competition for the given channel
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
  public DiscordServer getServers(@PathVariable("serverId") long serverId) {
    return discordService.getServer(serverId);
  }

  @GetMapping("/servers")
  public List<DiscordServer> getServers() {
    return discordService.getServers();
  }
}
