package de.mephisto.vpin.server.vr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vr")
public class VRResource {
  private final static Logger LOG = LoggerFactory.getLogger(VRResource.class);

  @Autowired
  private VRService vrService;

  @GetMapping("toggle")
  public boolean toggleVR() throws Exception {
    boolean b = vrService.toggleVRMode();
    LOG.info("VR Mode enabled: {}", b);
    return b;
  }
}
