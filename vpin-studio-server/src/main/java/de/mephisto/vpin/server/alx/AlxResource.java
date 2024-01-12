package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "alx")
public class AlxResource {
  @Autowired
  private AlxService analyticsService;

  @GetMapping
  public AlxSummary getAlxSummary() {
    return analyticsService.getAlxSummary();
  }

  @GetMapping("/{gameId}")
  public AlxSummary getAlxSummary(@PathVariable("gameId") int gameId) {
    return analyticsService.getAlxSummary(gameId);
  }
}
