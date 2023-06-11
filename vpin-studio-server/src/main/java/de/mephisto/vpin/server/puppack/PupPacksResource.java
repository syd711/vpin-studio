package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.client.CommandOption;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.PupPackRepresentation;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Date;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "puppacks")
public class PupPacksResource {
  private final static Logger LOG = LoggerFactory.getLogger(PupPacksResource.class);

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private GameService gameService;

  @GetMapping("/{gameId}")
  public PupPackRepresentation getPupPack(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      PupPack pupPack = pupPacksService.getPupPack(game);
      if (pupPack != null) {
        return toPupPackRepresentation(pupPack);
      }
    }
    return null;
  }

  @GetMapping("/enabled/{id}")
  public boolean enable(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return pupPacksService.isPupPackEnabled(game);
    }
    return false;
  }

  @GetMapping("/set/{id}/{enable}")
  public boolean enable(@PathVariable("id") int id,
                        @PathVariable("enable") boolean enable) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return pupPacksService.setPupPackEnabled(game, enable);
    }
    return false;
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return pupPacksService.clearCache();
  }

  @PostMapping("/option/{id}")
  public JobExecutionResult option(@PathVariable("id") Integer id,
                                   @RequestBody CommandOption option) {

    Game game = gameService.getGame(id);
    if (game != null) {
      return pupPacksService.option(game, option.getCommand());
    }
    return JobExecutionResultFactory.empty();
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
        LOG.error("No game found for PUP pack upload.");
        return JobExecutionResultFactory.error("No game found for PUP pack upload.");
      }

      File out = File.createTempFile(FilenameUtils.getBaseName(file.getOriginalFilename()), ".zip");
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      return pupPacksService.installPupPack(game, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }

  private PupPackRepresentation toPupPackRepresentation(@NonNull PupPack pupPack) {
    PupPackRepresentation representation = new PupPackRepresentation();
    representation.setSize(pupPack.getSize());
    representation.setModificationDate(new Date(pupPack.getPupPackFolder().lastModified()));
    representation.setOptions(pupPack.getOptions());
    representation.setScreenDMDMode(pupPack.getScreenMode(PopperScreen.DMD));
    representation.setScreenBackglassMode(pupPack.getScreenMode(PopperScreen.BackGlass));
    representation.setScreenTopperMode(pupPack.getScreenMode(PopperScreen.Topper));
    representation.setScreenFullDMDMode(pupPack.getScreenMode(PopperScreen.FullDMD));
    representation.setMissingResources(pupPack.getMissingResources());
    representation.setSelectedOption(pupPack.getSelectedOption());
    return representation;
  }
}
