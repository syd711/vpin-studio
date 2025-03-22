package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "tournaments")
public class TournamentsResource {

  @Autowired
  private TournamentsService tournamentsService;

  @GetMapping("/synchronize")
  public boolean synchronize() {
    return tournamentsService.synchronize();
  }

  @PostMapping("/synchronize")
  public boolean synchronize(@RequestBody TournamentMetaData metaData) {
    return tournamentsService.synchronize(metaData);
  }
}
