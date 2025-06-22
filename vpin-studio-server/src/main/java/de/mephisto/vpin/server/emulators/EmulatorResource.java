package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameEmulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "emulators")
public class EmulatorResource {

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @GetMapping
  public List<GameEmulator> getGameEmulators() {
    return emulatorService.getValidatedGameEmulators();
  }

  @GetMapping("/altExeNames/{emulatorId}")
  public List<String> getAltExeNames(@PathVariable("emulatorId") int emulatorId) {
    return emulatorService.getAltExeNames(emulatorId);
  }

  @GetMapping("/backglassemulators")
  public List<GameEmulator> getBackglassGameEmulators() {
    return frontendStatusService.getBackglassGameEmulators();
  }

  @PostMapping("/save")
  public GameEmulator save(@RequestBody GameEmulator emulator) throws Exception {
    return emulatorService.save(emulator);
  }

  @DeleteMapping("/{emulatorId}")
  public boolean delete(@PathVariable("emulatorId") int emulatorId) throws Exception {
    return emulatorService.delete(emulatorId);
  }
  @GetMapping("/clearcache")
  public boolean clearCache() {
    return emulatorService.clearCache();
  }
}
