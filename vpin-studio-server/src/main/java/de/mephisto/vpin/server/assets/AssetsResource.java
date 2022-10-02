package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.directb2s.B2SImageRatio;
import de.mephisto.vpin.server.directb2s.B2SManager;
import edu.umd.cs.findbugs.annotations.Nullable;
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

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@RestController
@RequestMapping("/assets")
public class AssetsResource {
  private final static Logger LOG = LoggerFactory.getLogger(AssetsResource.class);

  @Autowired
  private B2SManager directB2SManager;

  @Autowired
  private GameService service;

  @GetMapping("/directb2s/{id}/raw")
  public ResponseEntity<byte[]> getRaw(@PathVariable("id") int id) {
    File file = null;
    try {
      Game game = service.getGame(id);
      if (game != null) {
        file = directB2SManager.extractDirectB2SBackgroundImage(game);
        return serializeImage(file);
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
    return null;
  }

  @GetMapping("/directb2s/{id}/cropped/{ratio}")
  public ResponseEntity<byte[]> getCropped(@PathVariable("id") int id, @PathVariable("ratio") String ratio) {
    try {
      Game game = service.getGame(id);
      if (game != null) {
        B2SImageRatio r = B2SImageRatio.valueOf(ratio.toUpperCase());
        File file = directB2SManager.generateB2SImage(game, r, 1280);
        return serializeImage(file);
      }
      else {
        LOG.warn("No GameInfo found for id " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to load directb2s image: " + e.getMessage(), e);
    }
    return null;
  }


  private ResponseEntity<byte[]> serializeImage(@Nullable File file) throws Exception {
    BufferedInputStream in = null;
    if (file != null && file.exists()) {
      try {
        in = new BufferedInputStream(new FileInputStream(file));
        return ResponseEntity.ok()
            .lastModified(file.lastModified())
            .contentType(MediaType.parseMediaType("image/jpeg"))
            .contentLength(file.length())
            .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
            .body(IOUtils.toByteArray(in));
      } catch (Exception e) {
        LOG.error("Failed to serialize image " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        throw e;
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            //ignore
          }
        }
      }
    }
    else {
      if(file != null) {
        LOG.info("Image " + file.getAbsolutePath() + " not found.");
      }
    }
    return null;
  }
}
