package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.*;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "popper")
public class PopperServiceResource {
  private final static Logger LOG = LoggerFactory.getLogger(PopperServiceResource.class);

  @Autowired
  private PopperService popperService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @GetMapping("/custompoptions")
  public PopperCustomOptions getCustomOptions() {
    return popperService.getCustomOptions();
  }

  @GetMapping("/version")
  public int getVersion() {
    return popperService.getVersion();
  }

  @PostMapping("/custompoptions")
  public PopperCustomOptions getCustomOptions(@RequestBody PopperCustomOptions customOptions) {
    return popperService.saveCustomOptions(customOptions);
  }

  @GetMapping("/imports")
  public GameList getImportTables() {
    return popperService.getImportTables();
  }

  @PostMapping("/import")
  public JobExecutionResult importTable(@RequestBody GameListItem item) {
    GameEmulator emulator = pinUPConnector.getGameEmulator(item.getEmuId());
    File tableFile = new File(emulator.getTablesFolder(), item.getName());
    if (tableFile.exists()) {
      int result = popperService.importVPXGame(tableFile, true, -1, item.getEmuId());
      if (result > 0) {
        gameService.scanGame(result);
      }
    }
    return JobExecutionResultFactory.ok("Imported " + item.getName(), -1);
  }

  @GetMapping("/pincontrol/{screen}")
  public PinUPControl getPinUPControlFor(@PathVariable("screen") String screenName) {
    return popperService.getPinUPControlFor(PopperScreen.valueOf(screenName));
  }

  @GetMapping("/pincontrols")
  public PinUPControls getPinUPControls() {
    return popperService.getPinUPControls();
  }

  @GetMapping("/emulators")
  public List<GameEmulator> getGameEmulators() {
    return popperService.getGameEmulators();
  }

  @GetMapping("/backglassemulators")
  public List<GameEmulator> getBackglassGameEmulators() {
    return popperService.getBackglassGameEmulators();
  }

  @GetMapping("/running")
  public boolean isRunning() {
    return popperService.isPinUPRunning();
  }

  @GetMapping("/manager")
  public TableManagerSettings getArchiveManagerDescriptor() {
    return popperService.getArchiveManagerDescriptor();
  }

  @GetMapping("/terminate")
  public boolean terminate() {
    return popperService.terminate();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return popperService.restart();
  }

  @GetMapping("/tabledetails/{gameId}")
  public TableDetails getTableDetails(@PathVariable("gameId") int gameId) {
    return popperService.getTableDetails(gameId);
  }

  @GetMapping("/screen/{name}")
  public PinUPPlayerDisplay getScreen(@PathVariable("name") String name) {
    PopperScreen screen = PopperScreen.valueOf(name);
    return popperService.getPupPlayerDisplay(screen);
  }

  @PutMapping("/tabledetails/autofill/{gameId}/{overwrite}/{simulate}")
  public TableDetails autofill(@PathVariable("gameId") int gameId,
                               @PathVariable("overwrite") boolean overwrite) {
    TableDetails tableDetails = pinUPConnector.getTableDetails(gameId);
    return popperService.autoFill(gameService.getGame(gameId), tableDetails, overwrite, false);
  }

  @PostMapping("/tabledetails/autofillsimulate/{gameId}")
  public TableDetails autofill(@PathVariable("gameId") int gameId,
                               @RequestBody TableDetails tableDetails) {
    return popperService.autoFill(gameService.getGame(gameId), tableDetails, true, true);
  }

  @PostMapping("/tabledetails/{gameId}")
  public TableDetails save(@PathVariable("gameId") int gameId, @RequestBody TableDetails tableDetails) {
    return popperService.saveTableDetails(tableDetails, gameId, true);
  }
}
