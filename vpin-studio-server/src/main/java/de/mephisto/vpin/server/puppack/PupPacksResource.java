package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.restclient.JobExecutionResult;
import de.mephisto.vpin.restclient.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.representations.PupPackRepresentation;
import de.mephisto.vpin.server.altsound.AltSoundResource;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.puppack.PupPack;
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
      return toPupPackRepresentation(pupPacksService.getPupPack(game));
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

  @PostMapping("/upload")
  public JobExecutionResult upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam(value = "uploadType", required = false) String uploadType,
                                   @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.create("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for PUP pack upload.");
        return JobExecutionResultFactory.create("No game found for PUP pack upload.");
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
    return representation;
  }
}
