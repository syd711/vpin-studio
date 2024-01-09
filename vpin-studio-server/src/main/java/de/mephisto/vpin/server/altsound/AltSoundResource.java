package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameValidationService;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "altsound")
public class AltSoundResource {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundResource.class);

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameValidationService validationService;

  @GetMapping("{id}")
  public AltSound getAltSound(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return getAltSound(game);
    }
    return new AltSound();
  }

  @PostMapping("/save/{id}")
  public AltSound save(@PathVariable("id") int id, @RequestBody AltSound altSound) throws Exception {
    Game game = gameService.getGame(id);
    if (game != null) {
      altSoundService.save(game, altSound);
      return getAltSound(game);
    }
    return new AltSound();
  }

  @GetMapping("/restore/{id}")
  public AltSound restore(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return altSoundService.restore(game);
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

  @GetMapping("/stream/{emuId}/{name}/{filename}")
  public ResponseEntity<Resource> handleRequestWithName(@PathVariable("emuId") int emuId, @PathVariable("name") String altSoundName, @PathVariable("filename") String filename) throws IOException {
    AltSound altSound = altSoundService.getAltSound(emuId, altSoundName);
    File file = new File(altSound.getCsvFile().getParentFile(), filename);
    if (file.exists()) {
      FileInputStream in = new FileInputStream(file);
      byte[] bytes = IOUtils.toByteArray(in);
      ByteArrayResource bytesResource = new ByteArrayResource(bytes);
      in.close();

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.set(CONTENT_LENGTH, String.valueOf(file.length()));
      responseHeaders.set(CONTENT_TYPE, "audio/ogg");
      responseHeaders.set("Access-Control-Allow-Origin", "*");
      responseHeaders.set("Access-Control-Expose-Headers", "origin, range");
      responseHeaders.set("Cache-Control", "public, max-age=3600");
      return ResponseEntity.ok().headers(responseHeaders).body(bytesResource);
    }

    return ResponseEntity.notFound().build();
  }

  private AltSound getAltSound(@NonNull Game game) {
    AltSound altSound = altSoundService.getAltSound(game);
    altSound.setValidationStates(validationService.validateAltSound(game));
    return altSound;
  }
}
