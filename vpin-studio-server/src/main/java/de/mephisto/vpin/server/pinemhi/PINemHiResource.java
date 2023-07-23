package de.mephisto.vpin.server.pinemhi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "pinemhi")
public class PINemHiResource {
  private final static Logger LOG = LoggerFactory.getLogger(PINemHiResource.class);

  @Autowired
  private PINemHiService piNemHiService;

  @GetMapping("/settings")
  public Map<String, Object> settings() {
    return piNemHiService.loadSettings();
  }

  @GetMapping("/autostart")
  public boolean autostart() {
    return piNemHiService.getAutoStart();
  }

  @GetMapping("/running")
  public boolean isProcessRunning() {
    return piNemHiService.isRunning();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return piNemHiService.restart();
  }

  @GetMapping("/autostart/toggle")
  public boolean toggleAutoStart() {
    return piNemHiService.toggleAutoStart();
  }

  @GetMapping("/kill")
  public boolean kill() {
    return piNemHiService.kill();
  }

  @PostMapping("/save")
  public Map<String, Object> save(@RequestBody Map<String, Object> settings) throws Exception {
    return piNemHiService.save(settings);
  }
}
