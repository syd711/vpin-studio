package de.mephisto.vpin.server.fx.overlay;

import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.system.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class OverlayGenerator {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayGenerator.class);

  public final static File GENERATED_OVERLAY_FILE = new File(SystemInfo.RESOURCES, "overlay.jpg");

  private final GameService service;

  public static void generateOverlay(GameService service) throws Exception {
    new OverlayGenerator(service).generate();
  }

  OverlayGenerator(GameService service) {
    this.service = service;
  }

  public BufferedImage generate() throws Exception {
    try {
      BufferedImage bufferedImage = new OverlayGraphics().drawGames(service);
      ImageUtil.write(bufferedImage, GENERATED_OVERLAY_FILE);
      return bufferedImage;
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
  }
}
