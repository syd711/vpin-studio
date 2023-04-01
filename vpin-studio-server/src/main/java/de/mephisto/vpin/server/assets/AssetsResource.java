package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.RequestUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

  @Autowired
  private DefaultPictureService defaultPictureService;


  @GetMapping
  public List<Asset> getAssets() {
    return assetService.getAssets();
  }


  @GetMapping("/competition/{gameId}")
  public ResponseEntity<byte[]> getCompetitionBackground(@PathVariable("gameId") int gameId) throws Exception {
    try {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        Asset asset = assetService.getCompetitionBackground(gameId);
        if (asset == null) {
          BufferedImage background = defaultPictureService.generateCompetitionBackgroundImage(game, 800, 340);
          if (background != null) {
            byte[] bytes = ImageUtil.toBytes(background);
            asset = assetService.saveOrUpdate(bytes, -1, "image.png", AssetType.COMPETITION.name(), String.valueOf(game.getId()));
            LOG.info("Generated new competition background asset " + asset.getId());

            return assetService.serializeAsset(asset);
          }
        }
        else {
          return assetService.serializeAsset(asset);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed generate competition image: " + e.getMessage(), e);
    }

    return RequestUtil.serializeImage(new File(SystemService.RESOURCES, "competition-bg-default.png"));
  }

  @GetMapping("/defaultbackground/{id}")
  public ResponseEntity<byte[]> getRaw(@PathVariable("id") int id) throws Exception {
    try {
      Game game = gameService.getGame(id);
      if (game != null) {
        File target = game.getRawDefaultPicture();
        if (target != null && !target.exists()) {
          defaultPictureService.extractDefaultPicture(game);
        }

        target = game.getRawDefaultPicture();
        if (target != null && target.exists()) {
          return RequestUtil.serializeImage(target);
        }
      }
      else {
        LOG.warn("No GameInfo found for id " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to load directb2s image: " + e.getMessage(), e);
    }

    InputStream in = ResourceLoader.class.getResourceAsStream("empty-b2s-preview.png");
    byte[] bytes = IOUtils.toByteArray(in);
    return RequestUtil.serializeImage(bytes, "empty-b2s-preview.png");
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
