package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.*;
import edu.umd.cs.findbugs.annotations.NonNull;
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

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private FrontendService frontendService;

  @GetMapping("{id}")
  public AltSound getAltSound(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return getAltSound(game);
    }
    return new AltSound();
  }

  @GetMapping("{id}/fileinfo")
  public FileInfo getAltSoundFolder(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return game != null ? FileInfo.folder(altSoundService.getAltSoundFolder(game), game.getEmulator().getAltSoundFolder()) : null;
  }

  @DeleteMapping("{id}")
  public boolean delete(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return altSoundService.delete(game);
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return altSoundService.clearCache();
  }

  @PostMapping("/save/{id}")
  public boolean save(@PathVariable("id") int id, @RequestBody AltSound altSound) throws Exception {
    Game game = gameService.getGame(id);
    if (game != null) {
      altSoundService.save(game, altSound);
      return true;
    }
    return false;
  }

  @GetMapping("/restore/{id}")
  public boolean restore(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return altSoundService.restore(game);
  }

  @GetMapping("/mode/{id}")
  public int getAltSoundMode(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.getAltSoundMode(game);
    }
    return -1;
  }

  @PostMapping("/upload")
  public UploadDescriptor upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam("objectId") Integer emulatorId) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    descriptor.setEmulatorId(emulatorId);
    try {
      descriptor.upload();

      UploaderAnalysis analysis = new UploaderAnalysis(frontendService.supportPupPacks(), new File(descriptor.getTempFilename()));
      analysis.analyze();

      universalUploadService.importArchiveBasedAssets(descriptor, analysis, AssetType.ALT_SOUND);
      gameService.resetUpdate(analysis.getRomFromAltSoundPack(), VpsDiffTypes.altSound);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.ALT_SOUND.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.ALT_SOUND + " upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
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
    if (altSound != null) {
      altSound.setValidationStates(validationService.validateAltSound(game));
    }
    return altSound;
  }
}
