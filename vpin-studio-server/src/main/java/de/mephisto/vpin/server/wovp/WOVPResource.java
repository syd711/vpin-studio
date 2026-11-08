package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.connectors.wovp.models.WovpPlayer;
import de.mephisto.vpin.restclient.wovp.WovpPatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "wovp")
public class WOVPResource {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPResource.class);

  @Autowired
  private WovpService wovpService;

  @Autowired
  private WovpPatchService wovpPatchService;

  @GetMapping("/test/{apiKey}")
  public ApiKeyValidationResponse isValid(@PathVariable("apiKey") String apiKey) {
    return wovpService.validateKey(apiKey);
  }

  @GetMapping("/players")
  public List<WovpPlayer> getPlayers() {
    return wovpService.getPlayers();
  }

  @GetMapping("/clearCache")
  public boolean clearCache() {
    return wovpService.clearCache();
  }

  @GetMapping("/patch/{gameId}")
  public boolean isPatched(@PathVariable("gameId") int gameId) {
    return wovpPatchService.isPatched(gameId);
  }

  @PostMapping("/patch/{gameId}")
  public WovpPatchResult patch(@PathVariable("gameId") int gameId, @RequestParam(value = "backup", defaultValue = "true") boolean backup) {
    return wovpPatchService.patch(gameId, backup);
  }

  @PostMapping("/unpatch/{gameId}")
  public WovpPatchResult unpatch(@PathVariable("gameId") int gameId, @RequestParam(value = "backup", defaultValue = "false") boolean backup) {
    return wovpPatchService.unpatch(gameId, backup);
  }
}
