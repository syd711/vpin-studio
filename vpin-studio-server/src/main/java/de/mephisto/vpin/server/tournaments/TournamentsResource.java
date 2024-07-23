package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.restclient.tournaments.TournamentConfig;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
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
  public TournamentConfig getConfig() throws Exception {
    return tournamentsService.getConfig();
  }

  @GetMapping("/synchronize")
  public boolean synchronize() {
    return tournamentsService.synchronize();
  }

  @GetMapping
  public TournamentSettings getSettings() {
    return tournamentsService.getSettings();
  }

  @PostMapping
  public TournamentSettings save(@RequestBody TournamentSettings settings) {
    return tournamentsService.saveSettings(settings);
  }

  @PostMapping("/synchronize")
  public boolean synchronize(@RequestBody TournamentMetaData metaData) {
    return tournamentsService.synchronize(metaData);
  }
}
