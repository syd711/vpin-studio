package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vr")
public class VRResource {
  private final static Logger LOG = LoggerFactory.getLogger(VRResource.class);

  @Autowired
  private VRService vrService;

  @GetMapping("toggle")
  public boolean toggleVR() {
    boolean b = vrService.toggleVRMode();
    LOG.info("VR Mode enabled: {}", b);
    return b;
  }

  @GetMapping("launchscript/{emulatorId}")
  public GameEmulatorScript getEmulatorVRLaunchScript(@PathVariable("emulatorId") int emulatorId) {
    return vrService.getEmulatorVRLaunchScript(emulatorId);
  }

  @PostMapping("/save/{emulatorId}")
  public GameEmulatorScript saveVrLaunchScript(@PathVariable("emulatorId") int emulatorId,
                                               @RequestBody GameEmulatorScript script) {
    return vrService.saveVRLaunchScript(emulatorId, script);
  }
}
