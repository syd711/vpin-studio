package de.mephisto.vpin.server.wovp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "wovp")
public class WOVPResource {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPResource.class);

  @Autowired
  private WovpService wovpService;

  @GetMapping("/test")
  public String isValid() {
    return wovpService.validateKey();
  }
}
