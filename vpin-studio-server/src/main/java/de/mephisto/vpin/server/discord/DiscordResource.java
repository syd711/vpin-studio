package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordClientStatus;
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
  private DiscordClientService discordClientService;

  @GetMapping("/client/status")
  public DiscordClientStatus getClientStatus() {
    return discordClientService.getStatus();
  }

  @GetMapping("/client/launch")
  public boolean launchClient() {
    return discordClientService.launchMinimized();
  }

  @GetMapping("/validate")
  public DiscordBotStatus validateSettings() {
    return discordService.validateSettings();
  }

  @GetMapping("/permissions/competitions/managechannel/{serverId}/{channelId}")
  public boolean hasManagePermissions(@PathVariable("serverId") long serverId, @PathVariable("channelId") long channelId) {
    return discordService.hasManageChannelPermissions(serverId, channelId, discordService.getBotId());
  }

  @GetMapping("/permissions/competitions/managechannel/{serverId}")
  public boolean hasManagePermissions(@PathVariable("serverId") long serverId) {
    return discordService.hasManageChannelPermissions(serverId, discordService.getBotId());
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

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return discordService.clearCache();
  }
}
