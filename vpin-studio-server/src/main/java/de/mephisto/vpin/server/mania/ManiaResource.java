package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.restclient.mania.ManiaConfig;
import de.mephisto.vpin.restclient.mania.ManiaHighscoreSyncResult;
import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.server.games.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "mania")
public class ManiaResource {

  @Autowired
  private ManiaService maniaService;

  @GetMapping("/config")
  public ManiaConfig getConfig() throws Exception {
    return maniaService.getConfig();
  }


  @GetMapping("/clearcache")
  public boolean clearCache() throws Exception {
    return maniaService.clearCache();
  }

  @GetMapping("/scoresync/{vpsTableId}")
  public ManiaHighscoreSyncResult synchronizeHighscores(@PathVariable("vpsTableId") String vpsTableId) {
    return maniaService.synchronizeHighscores(vpsTableId);
  }

  @PostMapping("/register")
  public ManiaRegistration register(@RequestBody ManiaRegistration registration) throws Exception {
    return maniaService.register(registration);
  }
}
