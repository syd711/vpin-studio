package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "directb2s")
public class DirectB2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SResource.class);

  @Autowired
  private DirectB2SService directB2SManager;

  @Autowired
  private GameService service;

  @Autowired
  private AssetService assetService;

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> getRaw(@PathVariable("id") int id) throws Exception {
    File file = null;
    try {
      Game game = service.getGame(id);
      if (game != null) {
        file = directB2SManager.extractDirectB2SBackgroundImage(game);
        if (file != null && file.exists()) {
          return RequestUtil.serializeImage(file);
        }
      }
      else {
        LOG.warn("No GameInfo found for id " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to load directb2s image: " + e.getMessage(), e);
    } finally {
      if (file != null) {
        file.delete();
      }
    }
    return RequestUtil.serializeImage(new File(SystemService.RESOURCES, "empty-b2s-preview.png"));
  }

  @GetMapping("/competition/{gameId}")
  public ResponseEntity<byte[]> getCompetitionBackground(@PathVariable("gameId") int gameId) throws Exception {
    try {
      Game game = service.getGame(gameId);
      if (game != null) {
        Asset asset = assetService.getCompetitionBackground(gameId);
        if (asset == null) {
          BufferedImage background = directB2SManager.generateB2SCompetitionImage(game, 800, 340);
          if (background != null) {
            byte[] bytes = ImageUtil.toBytes(background);
            asset = assetService.saveOrUpdate(bytes, -1, "image.png", AssetType.COMPETITION.name());
            LOG.info("Generated new competition background asset " + asset.getId());

            return assetService.serializeAsset(asset);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed generate competition image: " + e.getMessage(), e);
    }

    return RequestUtil.serializeImage(new File(SystemService.RESOURCES, "competition-bg-default.png"));
  }
}
