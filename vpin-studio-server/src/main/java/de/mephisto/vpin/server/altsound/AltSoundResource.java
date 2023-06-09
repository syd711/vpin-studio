package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.AltSound;
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
@RequestMapping(API_SEGMENT + "altsound")
public class AltSoundResource {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundResource.class);

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private GameService gameService;

  @GetMapping("{id}")
  public AltSound csv(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.getAltSound(game);
    }
    return new AltSound();
  }

  @PostMapping("/save/{id}")
  public AltSound save(@PathVariable("id") int id, @RequestBody AltSound altSound) throws Exception {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.save(game, altSound);
    }
    return new AltSound();
  }

  @GetMapping("/restore/{id}")
  public AltSound restore(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.restore(game);
    }
    return new AltSound();
  }

  @GetMapping("/enabled/{id}")
  public boolean enable(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.isAltSoundEnabled(game);
    }
    return false;
  }

  @GetMapping("/set/{id}/{enable}")
  public boolean enable(@PathVariable("id") int id,
                        @PathVariable("enable") boolean enable) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.setAltSoundEnabled(game, enable);
    }
    return false;
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return altSoundService.clearCache();
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
        LOG.error("No game found for alt sound upload.");
        return JobExecutionResultFactory.error("No game found for alt sound upload.");
      }

      File out = File.createTempFile(FilenameUtils.getBaseName(file.getOriginalFilename()), ".zip");
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      return altSoundService.installAltSound(game, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }
}
