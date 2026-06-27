package de.mephisto.vpin.server.doftester;

import de.mephisto.vpin.restclient.doftester.ToySummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "doftester")
public class DOFTesterResource {
  private static final Logger LOG = LoggerFactory.getLogger(DOFTesterResource.class);

  @Autowired
  private DOFTesterService dofTesterService;

  @GetMapping("/toys/{gameId}")
  public ToySummary getToys(@PathVariable("gameId") int gameId) {
    return dofTesterService.getToys(gameId);
  }

  @PostMapping("/test/{gameId}/{toyName}")
  public boolean testToy(@PathVariable("gameId") int gameId,
                         @PathVariable("toyName") String toyName,
                         @RequestParam(value = "durationMs", defaultValue = "200") int durationMs) {
    LOG.info("DOF test request: gameId={}, toy='{}', durationMs={}", gameId, toyName, durationMs);
    return dofTesterService.testToy(gameId, toyName, durationMs);
  }

  @GetMapping("/clearcache")
  public boolean reloadConfig() {
    dofTesterService.invalidateCache();
    return true;
  }
}
