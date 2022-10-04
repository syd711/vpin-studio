package de.mephisto.vpin.server.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "system")
public class SystemResource {
  private final static Logger LOG = LoggerFactory.getLogger(SystemResource.class);

  @GetMapping("/exit")
  public void exit() {
    LOG.info("System exit called.");
    System.exit(0);
  }

  @GetMapping("/ping")
  public boolean ping() {
    return true;
  }

  @GetMapping("/restart")
  public boolean restart() {
    return true;
  }
}
