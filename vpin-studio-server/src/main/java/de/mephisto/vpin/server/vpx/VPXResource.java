package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "vpx")
public class VPXResource {
  private final static Logger LOG = LoggerFactory.getLogger(VPXResource.class);

  @Autowired
  private VPXService vpxService;

  @Autowired
  private GameService gameService;

  @GetMapping("/script/{id}")
  public String script(@PathVariable("id") int id) {
    return vpxService.getScript(gameService.getGame(id));
  }

  @GetMapping("/checksum/{id}")
  public String checksum(@PathVariable("id") int id) {
    return vpxService.getChecksum(gameService.getGame(id));
  }

  @GetMapping("/tableinfo/{id}")
  public TableInfo tableInfo(@PathVariable("id") int id) {
    return vpxService.getTableInfo(gameService.getGame(id));
  }

  @GetMapping("/sources/{id}")
  public String sources(@PathVariable("id") int id) {
    return vpxService.getSources(gameService.getGame(id));
  }

  @PutMapping("/sources/{id}")
  public boolean saveSources(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    String base64Source = (String) values.get("source");
    return vpxService.saveSources(gameService.getGame(id), base64Source);
  }

  @GetMapping("/pov/{id}")
  public POV getPov(@PathVariable("id") int id) {
    return vpxService.getPOV(gameService.getGame(id));
  }

  @PutMapping("/pov/{id}")
  public boolean put(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    return vpxService.savePOVPreference(gameService.getGame(id), values);
  }

  @PutMapping("/play/{id}")
  public boolean play(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    return vpxService.play(gameService.getGame(id));
  }

  @PostMapping("/pov/{id}")
  public POV create(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    return vpxService.create(gameService.getGame(id));
  }

  @DeleteMapping("/pov/{id}")
  public boolean delete(@PathVariable("id") int id) {
    return vpxService.delete(gameService.getGame(id));
  }


  @PostMapping("/pov/upload")
  public JobExecutionResult povUpload(@RequestParam(value = "file", required = false) MultipartFile file,
                                      @RequestParam(value = "uploadType", required = false) String uploadType,
                                      @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found POV upload.");
        return JobExecutionResultFactory.error("No game found for POV upload.");
      }

      String name = FilenameUtils.getBaseName(game.getGameFileName());
      File out = File.createTempFile(name, ".pov");
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);

      if (game.getPOVFile().exists() && !game.getPOVFile().delete()) {
        return JobExecutionResultFactory.error("Failed to delete " + game.getPOVFile().getAbsolutePath());
      }

      FileUtils.copyFile(out, game.getPOVFile());
      out.deleteOnExit();
      out.delete();

      return JobExecutionResultFactory.empty();
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "POV upload failed: " + e.getMessage());
    }
  }
}
