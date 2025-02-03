package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.frontend.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "screens")
public class VPinScreenResource {
  private final static Logger LOG = LoggerFactory.getLogger(VPinScreenResource.class);

  @Autowired
  private VPinScreenService vpinSCreenService;

  @GetMapping("/screen/{name}")
  public FrontendPlayerDisplay getScreen(@PathVariable("name") String name) {
    VPinScreen screen = VPinScreen.valueOf(name);
    return vpinSCreenService.getScreenDisplay(screen);
  }

  /*
  @GetMapping("/screens")
  public List<FrontendPlayerDisplay> getScreens() {
    return vpinSCreenService.getScreenDisplays();
  }
  */
}
