package de.mephisto.vpin.server.b2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 * Legacy controller for b2s curl calls
 */
@RestController
@RequestMapping(API_SEGMENT + "b2s")
public class B2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(B2SResource.class);

  @GetMapping
  public boolean getPinUPControlFor(@RequestParam("type") String type,
                                    @RequestParam("number") String number,
                                    @RequestParam("value") String value) {
    System.out.println(type + "/" + number + "/"+ value);
    return true;
  }
}
