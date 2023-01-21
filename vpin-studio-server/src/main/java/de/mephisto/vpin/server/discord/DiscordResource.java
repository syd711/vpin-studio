package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.highscores.ScoreList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "discord")
public class DiscordResource {

  @Autowired
  private DiscordService discordService;

  @GetMapping("/available")
  public boolean isDiscordBotAvailable() {
    return discordService.isEnabled();
  }

}
