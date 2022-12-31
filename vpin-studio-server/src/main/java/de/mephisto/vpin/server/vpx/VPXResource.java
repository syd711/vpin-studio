package de.mephisto.vpin.server.vpx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpx")
public class VPXResource {
  private final static Logger LOG = LoggerFactory.getLogger(VPXResource.class);

  @Autowired
  private VPXService vpxService;

  @GetMapping("/script/{id}")
  public String script(@PathVariable("id") int id) {
    return vpxService.getScript(id);
  }

  @GetMapping("/pov/{id}")
  public POV getPov(@PathVariable("id") int id) {
    return vpxService.getPOV(id);
  }

  @PostMapping("/pov/{id}")
  public POV createPov(@PathVariable("id") int id) {
    return vpxService.createPOV(id);
  }

  @PostMapping("/pov/save")
  public POV save(@RequestBody POV pov) {
    return vpxService.save(pov);
  }
}
