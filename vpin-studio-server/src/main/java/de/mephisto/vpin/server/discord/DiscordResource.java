package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.DiscordChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "discord")
public class DiscordResource {

  @Autowired
  private DiscordService discordService;

  @GetMapping("/available")
  public boolean isDiscordBotAvailable() {
    return discordService.isEnabled();
  }

  @GetMapping("/botId")
  public String getBotId() {
    return discordService.getBotId();
  }


  @GetMapping("/channels")
  public List<DiscordChannel> getChannels() {
    return discordService.getChannels();
  }
}
