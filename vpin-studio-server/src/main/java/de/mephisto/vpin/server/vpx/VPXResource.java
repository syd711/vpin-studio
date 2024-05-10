package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "vpx")
public class VPXResource {
  private final static Logger LOG = LoggerFactory.getLogger(VPXResource.class);

  @Autowired
  private VPXService vpxService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PreferencesService preferencesService;

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
    String source = (String) values.get("source");
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    return vpxService.importVBS(gameService.getGame(id), source, serverSettings.isKeepVbsFiles());
  }


  @GetMapping("/screenshot/{id}")
  public ResponseEntity<Resource> handleRequestWithName(@PathVariable("id") int id) throws IOException {
    try {
      Game game = gameService.getGame(id);
      if (game != null) {
        File file = game.getGameFile();
        byte[] bytes = VPXUtil.readScreenshot(file);
        if (bytes == null || bytes.length == 0) {
          InputStream in = ResourceLoader.class.getResourceAsStream("empty-preview.png");
          bytes = IOUtils.toByteArray(in);
        }

        ByteArrayResource bytesResource = new ByteArrayResource(bytes);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(CONTENT_LENGTH, String.valueOf(bytes.length));
        responseHeaders.set(CONTENT_TYPE, "image/png");
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        responseHeaders.set("Access-Control-Expose-Headers", "origin, range");
        responseHeaders.set("Cache-Control", "public, max-age=3600");
        return ResponseEntity.ok().headers(responseHeaders).body(bytesResource);
      }
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      LOG.error("Screenshot extraction failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Screenshot extraction failed: " + e.getMessage());
    }
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

  @PostMapping("/music/upload")
  public Boolean uploadMusic(@RequestParam(value = "file", required = false) MultipartFile file) {
    try {
      if (file == null) {
        LOG.error("Music upload request did not contain a file object.");
        return false;
      }

      String name = file.getName();
      File out = File.createTempFile(name, ".zip");
      out.deleteOnExit();

      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      return vpxService.installMusic(out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Music upload failed: " + e.getMessage());
    }
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

      gameService.resetUpdate(gameId, VpsDiffTypes.pov);
      return JobExecutionResultFactory.empty();
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "POV upload failed: " + e.getMessage());
    }
  }

  @PostMapping("/ini/upload")
  public JobExecutionResult initUpload(@RequestParam(value = "file", required = false) MultipartFile file,
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
      File iniFile = game.getIniFile();
      if (iniFile.exists() && !iniFile.delete()) {
        return JobExecutionResultFactory.error("Failed to delete " + iniFile.getAbsolutePath());
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

      if (suffix.equalsIgnoreCase("ini")) {
        FileUtils.copyFile(tempFile, iniFile);
      }
      else if (suffix.equalsIgnoreCase("rar") || suffix.equalsIgnoreCase("zip")) {
        PackageUtil.unpackTargetFile(tempFile, iniFile, ".ini");
      }
      else {
        throw new UnsupportedOperationException("The uploaded file has an invalid suffix \"" + suffix + "\"");
      }
    } catch (Exception e) {
      LOG.error("Ini upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Ini upload failed: " + e.getMessage());
    }
    return JobExecutionResultFactory.empty();
  }
}
