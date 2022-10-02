package de.mephisto.vpin.server.generators;

import de.mephisto.vpin.server.directb2s.B2SResource;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.system.SystemInfo;
import de.mephisto.vpin.server.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.File;

@RestController
@RequestMapping("/generator")
public class GeneratorResource {
  private final static Logger LOG = LoggerFactory.getLogger(GeneratorResource.class);

  public final static File GENERATED_OVERLAY_FILE = new File(SystemInfo.RESOURCES, "overlay.jpg");

  @Autowired
  private GameService service;

  @Autowired
  private HighscoreService highscoreService;

  @GetMapping("/overlay")
  public ResponseEntity<byte[]> generateOverlay() throws Exception {
    generate();
    return B2SResource.serializeImage(GENERATED_OVERLAY_FILE);
  }

  private BufferedImage generate() throws Exception {
    try {
      BufferedImage bufferedImage = new OverlayGraphics(service, highscoreService).drawGames();
      ImageUtil.write(bufferedImage, GENERATED_OVERLAY_FILE);
      return bufferedImage;
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
  }
}
