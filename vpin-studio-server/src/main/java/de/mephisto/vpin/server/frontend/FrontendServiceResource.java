package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameVpsMatch;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "frontend")
public class FrontendServiceResource {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendServiceResource.class);

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameService gameService;

  @GetMapping("/type")
  public FrontendType getFrontendType() {
    return frontendService.getFrontendType();
  }

  @GetMapping("/custompoptions")
  public FrontendCustomOptions getCustomOptions() {
    return frontendStatusService.getCustomOptions();
  }

  @GetMapping("/version")
  public int getVersion() {
    return frontendStatusService.getVersion();
  }

  @PostMapping("/custompoptions")
  public FrontendCustomOptions getCustomOptions(@RequestBody FrontendCustomOptions customOptions) {
    return frontendStatusService.saveCustomOptions(customOptions);
  }

  @GetMapping("/imports")
  public GameList getImportTables() {
    return frontendStatusService.getImportTables();
  }

  @PostMapping("/import")
  public JobExecutionResult importTable(@RequestBody GameListItem item) {
    File tableFile = new File(item.getFileName());
    if (tableFile.exists()) {
      int result = frontendStatusService.importVPXGame(tableFile, true, -1, item.getEmuId());
      if (result > 0) {
        gameService.scanGame(result);
      }
    }
    return JobExecutionResultFactory.ok("Imported " + item.getFileName(), -1);
  }

  @GetMapping("/pincontrol/{screen}")
  public FrontendControl getPinUPControlFor(@PathVariable("screen") String screenName) {
    return frontendStatusService.getPinUPControlFor(VPinScreen.valueOf(screenName));
  }

  @GetMapping("/pincontrols")
  public FrontendControls getPinUPControls() {
    return frontendStatusService.getPinUPControls();
  }

  @GetMapping("/emulators")
  public List<GameEmulator> getGameEmulators() {
    return frontendStatusService.getGameEmulators();
  }

  @GetMapping("/backglassemulators")
  public List<GameEmulator> getBackglassGameEmulators() {
    return frontendStatusService.getBackglassGameEmulators();
  }

  @GetMapping("/running")
  public boolean isRunning() {
    return frontendStatusService.isPinUPRunning();
  }

  @GetMapping("/manager")
  public TableManagerSettings getArchiveManagerDescriptor() {
    return frontendStatusService.getArchiveManagerDescriptor();
  }

  @GetMapping("/terminate")
  public boolean terminate() {
    return frontendStatusService.terminate();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return frontendStatusService.restart();
  }

  @GetMapping("/tabledetails/{gameId}")
  public TableDetails getTableDetails(@PathVariable("gameId") int gameId) {
    return frontendStatusService.getTableDetails(gameId);
  }

  @GetMapping("/screen/{name}")
  public FrontendPlayerDisplay getScreen(@PathVariable("name") String name) {
    VPinScreen screen = VPinScreen.valueOf(name);
    return frontendStatusService.getPupPlayerDisplay(screen);
  }

  @GetMapping("/screens")
  public List<FrontendPlayerDisplay> getScreens() {
    return frontendStatusService.getPupPlayerDisplays();
  }

  @PostMapping("/tabledetails/vpsLink/{gameId}")
  public boolean vpsLink(@PathVariable("gameId") int gameId, @RequestBody GameVpsMatch vpsmatch) throws Exception {
    frontendStatusService.vpsLink(gameId, vpsmatch.getExtTableId(), vpsmatch.getExtTableVersionId());
    return true;
  }

  @PutMapping("/tabledetails/fixVersion/{gameId}")
  public boolean fixVersion(@PathVariable("gameId") int gameId, @RequestBody Map<String, String> data) throws Exception {
    frontendStatusService.fixGameVersion(gameId, data.get("version"));
    return true;
  }

  @PutMapping("/tabledetails/autofill/{gameId}/{overwrite}")
  public TableDetails autofill(@PathVariable("gameId") int gameId,
                               @PathVariable("overwrite") boolean overwrite) {
    TableDetails tableDetails = frontendStatusService.getTableDetails(gameId);
    return frontendStatusService.autoFill(gameService.getGame(gameId), tableDetails, overwrite, false);
  }

  @PostMapping("/tabledetails/autofillsimulate/{gameId}")
  public TableDetails autofill(@PathVariable("gameId") int gameId,
                               @RequestBody TableDetails tableDetails) {
    return frontendStatusService.autoFill(gameService.getGame(gameId), tableDetails, true, true);
  }

  @PostMapping("/tabledetails/{gameId}")
  public TableDetails save(@PathVariable("gameId") int gameId, @RequestBody TableDetails tableDetails) {
    return frontendStatusService.saveTableDetails(tableDetails, gameId, true);
  }

  @GetMapping("/tabledetails/automatch/{gameId}/{overwrite}")
  public GameVpsMatch autoMatch(@PathVariable("gameId") int gameId, @PathVariable("overwrite") boolean overwrite) {
    return frontendStatusService.autoMatch(gameService.getGame(gameId), overwrite);
  }

}
