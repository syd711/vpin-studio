package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
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

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "dmd")
public class DMDResource {
  private final static Logger LOG = LoggerFactory.getLogger(DMDResource.class);

  @Autowired
  private DMDService dmdService;

  @Autowired
  private GameService gameService;

  @GetMapping("{id}")
  public DMDPackage get(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return dmdService.getDMDPackage(game);
    }
    return new DMDPackage();
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return dmdService.clearCache();
  }

  @GetMapping("/freezy/{emulatorId}")
  public ComponentSummary getFreezySummary(@PathVariable("emulatorId") int emulatorId) {
    return dmdService.getFreezySummary(emulatorId);
  }

  @PostMapping("/upload")
  public JobExecutionResult upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam(value = "uploadType", required = false) String uploadType,
                                   @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for DMD bundle upload.");
        return JobExecutionResultFactory.error("No game found for DMD bundle upload.");
      }

      File out = File.createTempFile(FilenameUtils.getBaseName(file.getOriginalFilename()), ".zip");
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      return dmdService.installDMDPackage(game, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }
}
