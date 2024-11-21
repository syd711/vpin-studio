package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.server.assets.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


  @GetMapping("/background/{gameId}")
  public ResponseEntity<byte[]> getFullDMDBackground(@PathVariable("gameId") int gameId) throws Exception {
    return dmdPositionService.getFullDMDBackground(gameId);
  }
}
