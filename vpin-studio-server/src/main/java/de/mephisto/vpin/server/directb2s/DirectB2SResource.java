package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.PackageUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "directb2s")
public class DirectB2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SResource.class);

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private GameService gameService;

  @GetMapping("/{id}")
  public DirectB2SData getData(@PathVariable("id") int id) {
    return backglassService.getDirectB2SData(id);
  }

  @GetMapping("/{emulatorId}/{name}")
  public DirectB2SData getData(@PathVariable("emulatorId") int emulatorId, @PathVariable("name") String name) {
    return backglassService.getDirectB2SData(emulatorId, URLDecoder.decode(name, StandardCharsets.UTF_8));
  }

  @DeleteMapping("/{emulatorId}/{name}")
  public boolean deleteBackglass(@PathVariable("emulatorId") int emulatorId, @PathVariable("name") String name) {
    return backglassService.deleteBackglass(emulatorId, URLDecoder.decode(name, StandardCharsets.UTF_8));
  }

  @PutMapping("/{emulatorId}/{name}")
  public boolean updateBackglass(@PathVariable("emulatorId") int emulatorId,
                                 @PathVariable("name") String name,
                                 @RequestBody Map<String, Object> values) throws IOException {
    name = URLDecoder.decode(name, StandardCharsets.UTF_8);
    String newName = (String) values.get("newName");
    if (values.containsKey("newName") && !StringUtils.isEmpty(newName)) {
      return backglassService.rename(emulatorId, name, newName);
    }

    if (values.containsKey("duplicate")) {
      return backglassService.duplicate(emulatorId, name);
    }
    return false;
  }

  @GetMapping
  public List<DirectB2S> getBackglasses() {
    return backglassService.getBackglasses();
  }

  @GetMapping("/tablesettings/{gameId}")
  public DirectB2STableSettings getTableSettings(@PathVariable("gameId") int gameId) {
    return backglassService.getTableSettings(gameId);
  }

  @PostMapping("/tablesettings/{gameId}")
  public DirectB2STableSettings saveTableSettings(@PathVariable("gameId") int gameId, @RequestBody DirectB2STableSettings settings) {
    try {
      return backglassService.saveTableSettings(gameId, settings);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table not supported: " + e.getMessage());
    }
  }

  @GetMapping("/serversettings/{emuId}")
  public DirectB2ServerSettings getServerSettings(@PathVariable("emuId") int emuId) {
    return backglassService.getServerSettings(emuId);
  }

  @PostMapping("/serversettings/{emuId}")
  public DirectB2ServerSettings saveServerSettings(@PathVariable("emuId") int emuId, @RequestBody DirectB2ServerSettings settings) {
    try {
      return backglassService.saveServerSettings(emuId, settings);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving custom options failed: " + e.getMessage());
    }
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
      File directB2SFile = game.getDirectB2SFile();
      if (directB2SFile.exists() && !directB2SFile.delete()) {
        return JobExecutionResultFactory.error("Failed to delete " + directB2SFile.getAbsolutePath());
      }

      String originalFilename = FilenameUtils.getBaseName(file.getOriginalFilename());
      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      if (StringUtils.isEmpty(suffix)) {
        throw new UnsupportedOperationException("The uploaded file has no valid suffix \"" + suffix + "\"");
      }

      File tempFile = File.createTempFile(FilenameUtils.getBaseName(originalFilename), "." + suffix);
      LOG.info("Uploading " + tempFile.getAbsolutePath());

      boolean upload = UploadUtil.upload(file, tempFile);
      if (!upload) {
        return JobExecutionResultFactory.error("Upload failed, check logs for details.");
      }

      if (suffix.equalsIgnoreCase("directb2s")) {
        FileUtils.copyFile(tempFile, directB2SFile);
      }
      else if (suffix.equalsIgnoreCase("rar") || suffix.equalsIgnoreCase("zip")) {
        PackageUtil.unpackTargetFile(tempFile, directB2SFile, ".directb2s");
      }
      else {
        throw new UnsupportedOperationException("The uploaded file has an invalid suffix \"" + suffix + "\"");
      }
      gameService.resetUpdate(gameId, VpsDiffTypes.b2s);
    } catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "DirectB2S upload failed: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }
}
