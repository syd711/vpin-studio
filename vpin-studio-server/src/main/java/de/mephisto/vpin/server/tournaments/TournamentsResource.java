package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.ManiaServiceConfig;
import de.mephisto.vpin.server.competitions.Competition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "tournaments")
public class TournamentsResource {

  @Autowired
  private TournamentsService tournamentsService;

  @GetMapping("/config")
  public ManiaServiceConfig getConfig() {
    return tournamentsService.getConfig();
  }
}
