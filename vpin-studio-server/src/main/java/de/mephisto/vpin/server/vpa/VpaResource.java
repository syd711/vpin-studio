package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpa")
public class VpaResource {

  @Autowired
  private VpaService vpaService;

  @PostMapping("/export")
  public Boolean export(@RequestBody ExportDescriptor exportDescriptor) {
    return vpaService.export(exportDescriptor);
  }

  @GetMapping("/manifest/{id}")
  public VpaManifest getManifest(@PathVariable("id") int id) {
    return vpaService.getManifest(id);
  }

}
