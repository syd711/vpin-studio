package de.mephisto.vpin.server.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Legacy URls:
 * "curl -X POST --data-urlencode \"info=\" http://localhost:" + HttpServer.PORT + "/service/popperLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\"  --data-urlencode "dirEmu=[DIREMU]" http://localhost:" + HttpServer.PORT + "/service/gameLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\"  --data-urlencode "dirEmu=[DIREMU]" http://localhost:" + HttpServer.PORT + "/service/gameExit";
 */
@RestController
@RequestMapping("/service")
//do not add api version AND DO NOT CHANGE "service" segment (these are already stored in popper too)
public class FrontendStatusEventsResource {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusEventsResource.class);

  @Autowired
  private FrontendStatusService frontendStatusService;

  @PostMapping("/gameLaunch")
  public boolean gameLaunch(@RequestParam("table") String table, @RequestParam("emu") String emuDirOrName) {
    return frontendStatusService.gameLaunch(table, emuDirOrName);
  }

  /**
   * Only used for testing through postman
   */
  @PostMapping("/gameRelaunch")
  public boolean gameRelaunch(@RequestParam("table") String table, @RequestParam("emu") String emuDirOrName) {
    frontendStatusService.gameExit(table, emuDirOrName);
    return frontendStatusService.gameLaunch(table, emuDirOrName);
  }

  @PostMapping("/gameExit")
  public boolean gameExit(@RequestParam("table") String table, @RequestParam("emu") String emuDirOrName) {
    return frontendStatusService.gameExit(table, emuDirOrName);
  }

  //kept for legacy reasons, do not delete!
  @PostMapping("/popperLaunch")
  public boolean popperLaunch() {
    frontendStatusService.notifyFrontendLaunch();
    return true;
  }

  @PostMapping("/frontendLaunch")
  public boolean frontendLaunch() {
    frontendStatusService.notifyFrontendLaunch();
    return true;
  }

  @PostMapping("/menuUpdate")
  public boolean menuUpdate(@RequestBody Map<String, Object> parameters) {
    System.out.println(parameters);
    return true;
  }
}
