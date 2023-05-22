package de.mephisto.vpin.server.backup.adapters.vpbm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpbm")
public class VpbmResource {

  @Autowired
  private VpbmService vpbmService;

  @GetMapping("version")
  public String getVersion() {
    return vpbmService.getVersion();
  }

  @PostMapping("/update")
  public Boolean update() {
    return vpbmService.update();
  }

  @GetMapping("/updateavailable")
  public Boolean updateavailable() {
    return vpbmService.isUpdateAvailable();
  }

  @GetMapping("/hostids")
  public VpbmHosts getHostIds() {
    return vpbmService.getHostIds();
  }

  @PostMapping
  public VpbmHosts saveHostIds(@RequestBody VpbmHosts vpbmHosts) {
    return vpbmService.saveHostIds(vpbmHosts);
  }
}
