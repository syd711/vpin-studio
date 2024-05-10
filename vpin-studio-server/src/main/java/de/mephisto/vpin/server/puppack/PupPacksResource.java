package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.client.CommandOption;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.puppacks.PupPackRepresentation;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameValidationService;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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

  @Autowired
  private GameValidationService validationService;


  @DeleteMapping("{id}")
  public boolean delete(@PathVariable("id") int id) {
    return pupPacksService.delete(gameService.getGame(id));
  }

  @GetMapping("/menu")
  public PupPackRepresentation getPupPack() {
    PupPack pupPack = pupPacksService.getMenuPupPack();
    if (pupPack != null) {
      return toPupPackRepresentation(null, pupPack);
    }
    return null;
  }

  @GetMapping("/{gameId}")
  public PupPackRepresentation getPupPack(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      PupPack pupPack = pupPacksService.getPupPack(game);
      if (pupPack != null) {
        return toPupPackRepresentation(game, pupPack);
      }
    }
    return null;
  }

  @GetMapping("/enabled/{rom}")
  public boolean enable(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return !pupPacksService.isPupPackDisabled(game);
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

      String extension = FilenameUtils.getExtension(file.getOriginalFilename());
      File pupTempArchive = File.createTempFile(FilenameUtils.getBaseName(file.getOriginalFilename()), "." + extension);
      LOG.info("Uploading " + pupTempArchive.getAbsolutePath());
      UploadUtil.upload(file, pupTempArchive);
      JobExecutionResult jobExecutionResult = pupPacksService.installPupPack(game, pupTempArchive);
      gameService.resetUpdate(gameId, VpsDiffTypes.pupPack);
      return jobExecutionResult;
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "PUP pack upload failed: " + e.getMessage());
    }
  }

  private PupPackRepresentation toPupPackRepresentation(@Nullable Game game, @NonNull PupPack pupPack) {
    PupPackRepresentation representation = new PupPackRepresentation();
    representation.setSize(pupPack.getSize());
    representation.setScriptOnly(pupPack.isScriptOnly());
    representation.setPath(pupPack.getPupPackFolder().getPath().replaceAll("\\\\", "/"));
    representation.setModificationDate(new Date(pupPack.getPupPackFolder().lastModified()));
    representation.setOptions(pupPack.getOptions());
    representation.setScreenDMDMode(pupPack.getScreenMode(PopperScreen.DMD));
    representation.setScreenBackglassMode(pupPack.getScreenMode(PopperScreen.BackGlass));
    representation.setScreenTopperMode(pupPack.getScreenMode(PopperScreen.Topper));
    representation.setScreenFullDMDMode(pupPack.getScreenMode(PopperScreen.Menu));
    representation.setMissingResources(pupPack.getMissingResources());
    representation.setSelectedOption(pupPack.getSelectedOption());
    representation.setTxtFiles(pupPack.getTxtFiles());
    representation.setName(pupPack.getName());
    representation.setHelpTransparency(pupPack.isTransparent(PopperScreen.GameHelp));
    representation.setInfoTransparency(pupPack.isTransparent(PopperScreen.GameInfo));
    representation.setOther2Transparency(pupPack.isTransparent(PopperScreen.Other2));

    if (game != null) {
      representation.setEnabled(!pupPacksService.isPupPackDisabled(game));
      representation.setValidationStates(validationService.validatePupPack(game));
    }
    return representation;
  }
}
