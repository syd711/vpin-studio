package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.restclient.tournaments.TournamentConfig;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "tournaments")
public class TournamentsResource {

  @Autowired
  private TournamentsService tournamentsService;

  @GetMapping("/config")
  public TournamentConfig getConfig() {
    return tournamentsService.getConfig();
  }

  @GetMapping
  public TournamentSettings getSettings() {
    return tournamentsService.getSettings();
  }

  @PostMapping
  public TournamentSettings save(@RequestBody TournamentSettings settings) {
    return tournamentsService.saveSettings(settings);
  }
}
