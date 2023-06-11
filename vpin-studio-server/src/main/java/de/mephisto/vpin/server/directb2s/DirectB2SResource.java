package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.DirectB2SData;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "directb2s")
public class DirectB2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SResource.class);

  @Autowired
  private GameService gameService;

  @GetMapping("/{id}")
  public DirectB2SData getData(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if(game != null && game.isDirectB2SAvailable()) {
      DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
      return extractor.extractData(game.getDirectB2SFile());
    }
    return new DirectB2SData();
  }

  @PostMapping("/upload")
  public JobExecutionResult directb2supload(@RequestParam(value = "file", required = false) MultipartFile file,
                                            @RequestParam(value = "uploadType", required = false) String uploadType,
                                            @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for upload.");
        return JobExecutionResultFactory.error("No game found for upload.");
      }
      File out = game.getDirectB2SFile();
      if(out.exists() && !out.delete()) {
        return JobExecutionResultFactory.error("Failed to delete " + out.getAbsolutePath());
      }

      LOG.info("Uploading " + out.getAbsolutePath());
      boolean upload = UploadUtil.upload(file, out);
      if(!upload) {
        return JobExecutionResultFactory.error("Upload failed, check logs for details.");
      }
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "DirectB2S upload failed: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }
}
