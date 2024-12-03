package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.restclient.pinvol.PinVolTablePreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "pinvol")
public class PinVolResource {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolResource.class);

  @Autowired
  private PinVolService pinVolService;


  @GetMapping("/autostart")
  public boolean autostart() {
    return pinVolService.getPinVolAutoStart();
  }

  @GetMapping("/running")
  public boolean isProcessRunning() {
    return pinVolService.isRunning();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return pinVolService.restart();
  }

  @GetMapping("/autostart/toggle")
  public boolean toggleAutoStart() {
    return pinVolService.toggleAutoStart();
  }

  @GetMapping("/kill")
  public boolean kill() {
    return pinVolService.killPinVol();
  }

  @GetMapping("/preferences")
  public PinVolTablePreferences getPreferences() {
    return pinVolService.getPinVolTablePreferences();
  }

  @GetMapping("/setvolume")
  public boolean setVolume() {
    return pinVolService.setSystemVolume();
  }

}
