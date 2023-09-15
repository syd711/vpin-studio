package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PopperCustomOptions;
import de.mephisto.vpin.restclient.SystemData;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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

  @GetMapping("/custompoptions")
  public PopperCustomOptions getCustomOptions() {
    return popperService.getCustomOptions();
  }


  @PostMapping("/custompoptions")
  public PopperCustomOptions saveCustomOptions(@RequestBody PopperCustomOptions option) {
    try {
      return popperService.saveCustomOptions(option);
    } catch (Exception e) {
      throw new ResponseStatusException(CONFLICT, "Saving custom options failed: " + e.getMessage());
    }
  }

  @GetMapping("/imports")
  public SystemData getImportTables() {
    return popperService.getImportTables();
  }

  @PostMapping("/import")
  public JobExecutionResult importTables(@RequestBody SystemData resourceList) {
    return popperService.importTables(resourceList);
  }

  @GetMapping("/pincontrol/{screen}")
  public PinUPControl getPinUPControlFor(@PathVariable("screen") String screenName) {
    return popperService.getPinUPControlFor(PopperScreen.valueOf(screenName));
  }

  @GetMapping("/pincontrols")
  public PinUPControls getPinUPControls() {
    return popperService.getPinUPControls();
  }

  @GetMapping("/running")
  public boolean isRunning() {
    return popperService.isPinUPRunning();
  }

  @PostMapping("/manager")
  public boolean saveArchiveManager(@RequestBody TableManagerSettings descriptor) {
    return popperService.saveArchiveManager(descriptor);
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
  public TableDetails get(@PathVariable("gameId") int gameId) {
    return popperService.getTableDetails(gameId);
  }

  @PutMapping("/tabledetails/autofill/{gameId}")
  public TableDetails autofill(@PathVariable("gameId") int gameId) {
    return popperService.autofillTableDetails(gameService.getGame(gameId));
  }

  @PostMapping("/tabledetails/{gameId}")
  public TableDetails save(@PathVariable("gameId") int gameId, @RequestBody TableDetails tableDetails) {
    return popperService.saveTableDetails(tableDetails, gameId);
  }
}
