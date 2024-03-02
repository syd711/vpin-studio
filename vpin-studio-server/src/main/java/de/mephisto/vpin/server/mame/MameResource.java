package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.mame.MameOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "mame")
public class MameResource {

  @Autowired
  private MameService mameService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @GetMapping("/options/{rom}")
  public MameOptions getOptions(@PathVariable("rom") String rom) {
    return mameService.getOptions(rom);
  }

  @PostMapping("/options")
  public MameOptions saveOptions(@RequestBody MameOptions options) {
    return mameService.saveOptions(options);
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    mameRomAliasService.clearCache();
    return mameService.clearCache();
  }
}
