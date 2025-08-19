package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameCachingService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.resources.ResourceLoader;
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

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private GameCachingService gameCachingService;

  @GetMapping("/script/{id}")
  public String script(@PathVariable("id") int id) {
    return vpxService.getScript(gameService.getGame(id));
  }

  @GetMapping("/vpinballx")
  public String getVPinballX() {
    File vpxFile = vpxService.getVPXFile();
    if (vpxFile.exists()) {
      return vpxFile.getAbsolutePath();
    }
    return null;
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

  @PutMapping("/nvoffset/{gameId}")
  public int setNvOffset(@PathVariable("gameId") int gameId, @RequestBody Map<String, Object> values) throws Exception {
    int nvOffset = (Integer) values.get("nvOffset");
    Game game = gameService.getGame(gameId);
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    boolean replaced = vpxService.setNvOffset(game, nvOffset, serverSettings.isKeepVbsFiles());
    if (replaced) {
      gameCachingService.scanGame(game.getId());
      gameCachingService.invalidate(game.getId());
      return gameCachingService.getGame(game.getId()).getNvOffset();
    }
    return -1;
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
    }
    catch (Exception e) {
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

  @PostMapping("/pov/{id}")
  public POV create(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    return vpxService.create(gameService.getGame(id));
  }

  @DeleteMapping("/pov/{id}")
  public boolean delete(@PathVariable("id") int id) {
    return vpxService.delete(gameService.getGame(id));
  }

  @PostMapping("/music/upload")
  public UploadDescriptor uploadMusic(@RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    try {
      descriptor.setAcceptAllAudioAsMusic(true);
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.MUSIC);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("POV upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Music upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

  @PostMapping("/pov/upload")
  public UploadDescriptor povUpload(@RequestParam(value = "file", required = false) MultipartFile file,
                                    @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.upload();
      universalUploadService.importFileBasedAssets(descriptor, AssetType.POV);
      gameService.resetUpdate(gameId, VpsDiffTypes.pov);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("POV upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "POV upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }
}
