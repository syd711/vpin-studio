package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetService;
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
import java.io.InputStream;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "directb2s")
public class DirectB2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SResource.class);

  @Autowired
  private DefaultPictureService defaultPictureService;

  @Autowired
  private GameService gameService;

  @Autowired
  private AssetService assetService;

  @GetMapping("/{id}")
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

  @GetMapping("/competition/{gameId}")
  public ResponseEntity<byte[]> getCompetitionBackground(@PathVariable("gameId") int gameId) throws Exception {
    try {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        Asset asset = assetService.getCompetitionBackground(gameId);
        if (asset == null) {
          BufferedImage background = defaultPictureService.generateB2SCompetitionImage(game, 800, 340);
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

  @PostMapping("/upload")
  public Boolean directb2supload(@RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam(value = "uploadType", required = false) String uploadType,
                                 @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return false;
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for upload.");
        return false;
      }
      File out = game.getDirectB2SFile();
      LOG.info("Uploading " + out.getAbsolutePath());
      return UploadUtil.upload(file, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "DirectB2S upload failed: " + e.getMessage());
    }
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
