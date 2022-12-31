package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpx")
public class VPXResource {

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

  @PutMapping("/pov/{id}")
  public boolean put(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    return vpxService.savePOVPreference(id, values);
  }
}
