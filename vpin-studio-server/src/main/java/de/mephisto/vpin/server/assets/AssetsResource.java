package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.VPinService;
import de.mephisto.vpin.server.directb2s.DirectB2SManager;
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
@RequestMapping("/assets")
public class AssetsResource {
  private final static Logger LOG = LoggerFactory.getLogger(AssetsResource.class);

  @Autowired
  private DirectB2SManager directB2SManager;

  @Autowired
  private VPinService service;

  @GetMapping("/directb2s/{id}")
  public ResponseEntity<byte[]> get(@PathVariable("id") int id) {
    BufferedInputStream in = null;
    try {
      GameInfo gameInfo = service.getGameInfo(id);
      if (gameInfo != null) {
        File file = directB2SManager.extractDirectB2SBackgroundImage(gameInfo);
        if (file != null) {
          in = new BufferedInputStream(new FileInputStream(file));
          return ResponseEntity.ok()
              .lastModified(file.lastModified())
              .contentType(MediaType.parseMediaType("image/jpeg"))
              .contentLength(file.length())
              .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
              .body(IOUtils.toByteArray(in));
        }
        else {
          LOG.warn(gameInfo + " does not provide a directb2s background.");
        }
      }
      else {
        LOG.warn("No GameInfo found for id " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to load directb2s image: " + e.getMessage(), e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }

    return null;
  }
}
