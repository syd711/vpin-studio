package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.SystemData;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
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

  @GetMapping("/playlists")
  public List<Playlist> getPlaylists() {
    return popperService.getPlaylists();
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

  @PostMapping("/tabledetails/{gameId}")
  public TableDetails save(@PathVariable("gameId") int gameId, @RequestBody TableDetails tableDetails) {
    return popperService.saveTableDetails(tableDetails, gameId);
  }

  @PostMapping("/upload/{screen}")
  public JobExecutionResult upload(@PathVariable("screen") PopperScreen popperScreen,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam(value = "uploadType", required = false) String uploadType,
                                   @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for popper media upload.");
        return JobExecutionResultFactory.error("No game found for PinUP Popper media upload.");
      }

      File pinUPMediaFolder = game.getPinUPMediaFolder(popperScreen);
      String filename = game.getGameDisplayName();
      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());

      File out = new File(pinUPMediaFolder, filename + "." + suffix);
      if (out.exists()) {
        String nameIndex = "01";
        out = new File(pinUPMediaFolder, filename + nameIndex + "." + suffix);
      }

      int index = 1;
      while (out.exists()) {
        index++;
        String nameIndex = index <= 9 ? "0" + index : String.valueOf(index);
        out = new File(pinUPMediaFolder, filename + nameIndex + "." + suffix);
      }

      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);

      return JobExecutionResultFactory.empty();
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }

}
