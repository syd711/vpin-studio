package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> getRaw(@PathVariable("id") int id) throws Exception {
    File file = null;
    try {
      Game game = service.getGame(id);
      if (game != null) {
        file = directB2SManager.extractDirectB2SBackgroundImage(game);
        if(file != null && file.exists()) {
          return RequestUtil.serializeImage(file);
        }
      }
      else {
        LOG.warn("No GameInfo found for id " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to load directb2s image: " + e.getMessage(), e);
    }
    finally {
      if(file != null) {
        file.delete();
      }
    }
    return  RequestUtil.serializeImage(new File(SystemService.RESOURCES, "empty-b2s-preview.png"));
  }

  @GetMapping("/{id}/cropped/{ratio}")
  public ResponseEntity<byte[]> getCropped(@PathVariable("id") int id, @PathVariable("ratio") String ratio) {
    try {
      Game game = service.getGame(id);
      if (game != null) {
        DirectB2SImageRatio r = DirectB2SImageRatio.valueOf(ratio.toUpperCase());
        File file = directB2SManager.generateB2SImage(game, r, 1280);
        return RequestUtil.serializeImage(file);
      }
      else {
        LOG.warn("No GameInfo found for id " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to load directb2s image: " + e.getMessage(), e);
    }
    return null;
  }
}
