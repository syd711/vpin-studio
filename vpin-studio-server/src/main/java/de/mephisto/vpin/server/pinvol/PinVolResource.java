package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.pinvol.PinVolUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "pinvol")
public class PinVolResource {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolResource.class);

  @Autowired
  private PinVolService pinVolService;

  @GetMapping("/running")
  public boolean isProcessRunning() {
    return pinVolService.isRunning();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return pinVolService.restart();
  }

  @GetMapping("/kill")
  public boolean kill() {
    return pinVolService.killPinVol();
  }

  @GetMapping("/valid")
  public boolean isValid() {
    return pinVolService.isValid();
  }

  @GetMapping("/preferences")
  public PinVolPreferences getPreferences() {
    return pinVolService.getPinVolTablePreferences();
  }

  @GetMapping("/setvolume")
  public boolean setVolume() {
    return pinVolService.setSystemVolume();
  }

  @PostMapping("/save")
  public PinVolPreferences save(@RequestBody PinVolUpdate update) throws Exception {
    return pinVolService.save(update);
  }

}
