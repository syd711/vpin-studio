package de.mephisto.vpin.server.fx.overlay;

import de.mephisto.vpin.server.VPinService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class OverlayGenerator {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayGenerator.class);

  public final static File GENERATED_OVERLAY_FILE = new File(SystemInfo.RESOURCES, "overlay.jpg");

  private final VPinService service;

  public static void generateOverlay(VPinService service) throws Exception {
    new OverlayGenerator(service).generate();
  }

  OverlayGenerator(VPinService service) {
    this.service = service;
  }

  public BufferedImage generate() throws Exception {
    try {
      service.refreshGameInfos();
      BufferedImage bufferedImage = new OverlayGraphics().drawGames(service);
      ImageUtil.write(bufferedImage, GENERATED_OVERLAY_FILE);
      return bufferedImage;
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
  }
}
