package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.AltColor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.ValidationService;
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

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "altcolor")
public class AltColorResource {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorResource.class);

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private GameService gameService;

  @Autowired
  private ValidationService validationService;

  @GetMapping("{id}")
  public AltColor get(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return getAltColor(game);
    }
    return new AltColor();
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return altColorService.clearCache();
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
        LOG.error("No game found for alt color upload.");
        return JobExecutionResultFactory.error("No game found for alt color upload.");
      }

      String name = FilenameUtils.getBaseName(file.getOriginalFilename());
      String ext = FilenameUtils.getExtension(file.getOriginalFilename());
      File out = File.createTempFile(name, "." + ext);
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      return altColorService.installAltColor(game, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT color upload failed: " + e.getMessage());
    }
  }

  private AltColor getAltColor(@NonNull Game game) {
    AltColor altColor = altColorService.getAltColor(game);
    if(altColor != null) {
      altColor.setValidationStates(validationService.validateAltColor(game));
    }
    return altColor;
  }
}
