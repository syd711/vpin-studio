package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "assets")
public class AssetsResource {
  private final static Logger LOG = LoggerFactory.getLogger(AssetsResource.class);

  @Autowired
  private AssetService assetService;

  @Autowired
  private GameService gameService;

  @GetMapping
  public List<Asset> getAssets() {
    return assetService.getAssets();
  }

  @GetMapping("/{id}")
  public Asset getById(@PathVariable("id") final int id) {
    return assetService.getById(id);
  }

  @GetMapping("/data/{uuid}")
  public ResponseEntity<byte[]> get(@PathVariable("uuid") String uuid) {
    Asset asset = assetService.getByUuid(uuid);
    if (asset != null) {
      return assetService.serializeAsset(asset);
    }
    throw new ResponseStatusException(NOT_FOUND, "Not asset found for uuid " + uuid);
  }

  @DeleteMapping("/{id}")
  public boolean delete(@PathVariable("id") int id) {
    return assetService.delete(id);
  }

  @DeleteMapping("/background/{gameId}")
  public boolean deleteDefaultBackground(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if(game != null) {
      if (game.getCroppedDefaultPicture() != null && game.getCroppedDefaultPicture().exists()) {
        if(!game.getCroppedDefaultPicture().delete()) {
          LOG.error("Failed to delete default crop asset.");
        }
      }

      if (game.getRawDefaultPicture() != null && game.getRawDefaultPicture().exists()) {
        if(!game.getRawDefaultPicture().delete()) {
          LOG.error("Failed to delete default crop asset.");
        }
      }
    }
    return true;
  }

  @PostMapping("/save")
  public Asset save(@RequestBody Asset asset) {
    return assetService.save(asset);
  }

  @PostMapping("/{id}/upload/{max}")
  public Asset upload(@PathVariable("id") long id,
                      @PathVariable("max") int maxSize,
                      @RequestParam("assetType") String assetType,
                      @RequestParam("file") MultipartFile file) throws IOException {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      throw new ResponseStatusException(NOT_FOUND, "Upload request did not contain a file object.");
    }

    byte[] data = file.getBytes();
    if (maxSize > 0) {
      data = UploadUtil.resizeImageUpload(file, maxSize);
    }
    return assetService.saveOrUpdate(data, id, file.getOriginalFilename(), assetType, null);
  }

  @PostMapping("/background")
  public Boolean backgroundUpload(@RequestParam(value = "file", required = false) MultipartFile file,
                                  @RequestParam(value = "uploadType", required = false) String uploadType,
                                  @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return false;
      }

      Game game = gameService.getGame(gameId);
      if (game == null || game.getRawDefaultPicture() == null || game.getCroppedDefaultPicture() == null) {
        LOG.error("Invalid game data.");
        return false;
      }

      if (game.getCroppedDefaultPicture().exists()) {
        game.getCroppedDefaultPicture().delete();
      }

      if (game.getRawDefaultPicture().exists()) {
        game.getRawDefaultPicture().delete();
      }

      File out = game.getRawDefaultPicture();
      LOG.info("Uploading " + out.getAbsolutePath());
      return UploadUtil.upload(file, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Background image upload failed: " + e.getMessage());
    }
  }
}
