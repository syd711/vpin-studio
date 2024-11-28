package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "dmdposition")
public class DMDPositionResource {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionResource.class);

  @Autowired
  private DMDPositionService dmdPositionService;

  @GetMapping("/{gameId}")
  public DMDInfo getDMD(@PathVariable("gameId") int gameId) {
    return dmdPositionService.getDMDInfo(gameId);
  }

  @PostMapping("/move")
  public DMDInfo moveDMD(@RequestBody DMDInfo dmdInfo, @RequestParam VPinScreen target) {
    return dmdPositionService.moveDMDInfo(dmdInfo, target);
  }

  @PostMapping("/autoPosition")
  public DMDInfo autoPosition(@RequestBody DMDInfo dmdInfo) {
    return dmdPositionService.autoPositionDMDInfo(dmdInfo);
  }

  @PostMapping("/save")
  public boolean saveDMD(@RequestBody DMDInfo dmdInfo) {
    try {
      return dmdPositionService.saveDMDInfo(dmdInfo);
    }
    catch (Exception e) {
      LOG.error("Saving DMD position failed", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Saving DMD position failed: " + e.getMessage());
    }
  }
}
