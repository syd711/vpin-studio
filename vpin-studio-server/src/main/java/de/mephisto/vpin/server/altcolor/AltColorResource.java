package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameValidationService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "altcolor")
public class AltColorResource {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameValidationService validationService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @GetMapping("{id}")
  public AltColor get(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return getAltColor(game);
    }
    return new AltColor();
  }

  @GetMapping("{id}/fileinfo")
  public FileInfo getAltColorFolder(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return game != null ? FileInfo.folder(altColorService.getAltColorFolder(game), game.getEmulator().getAltColorFolder()) : null;
  }

  @DeleteMapping("/{id}/{filename}")
  public boolean deleteBackup(@PathVariable("id") int id, @PathVariable("filename") String filename) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altColorService.deleteBackup(game, filename);
    }
    return false;
  }

  @PutMapping("restore/{gameId}/{filename}")
  public boolean restore(@PathVariable("gameId") int gameId, @PathVariable("filename") String filename) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return altColorService.restore(game, filename);
    }
    return false;
  }

  @DeleteMapping("{id}")
  public boolean delete(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altColorService.delete(game);
    }
    return false;
  }

  @PostMapping("/upload")
  public UploadDescriptor upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.ALT_COLOR);
      gameService.resetUpdate(gameId, VpsDiffTypes.altColor);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.ALT_COLOR.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.ALT_COLOR.name() + " upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

  private AltColor getAltColor(@NonNull Game game) {
    AltColor altColor = altColorService.getAltColor(game);
    if (altColor.isAvailable()) {
      altColor.setValidationStates(validationService.validateAltColor(game));
    }
    return altColor;
  }
}
