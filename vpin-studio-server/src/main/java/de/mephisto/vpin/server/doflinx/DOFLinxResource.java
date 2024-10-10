package de.mephisto.vpin.server.doflinx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "doflinx")
public class DOFLinxResource {
  private final static Logger LOG = LoggerFactory.getLogger(DOFLinxResource.class);

  @Autowired
  private DOFLinxService dofLinxService;

  @GetMapping("/autostart")
  public boolean autostart() {
    return dofLinxService.getDOFLinxAutoStart();
  }

  @GetMapping("/running")
  public boolean isProcessRunning() {
    return dofLinxService.isRunning();
  }

  @GetMapping("/valid")
  public boolean isValid() {
    return dofLinxService.isValid();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return dofLinxService.restart();
  }

  @GetMapping("/autostart/toggle")
  public boolean toggleAutoStart() {
    return dofLinxService.toggleAutoStart();
  }

  @GetMapping("/kill")
  public boolean kill() {
    return dofLinxService.killDOFLinx();
  }

}
