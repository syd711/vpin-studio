package de.mephisto.vpin.server.overlay;

import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.overlay.fx.OverlayWindowFX;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OverlayService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayService.class);
  public static final String OVERLAY_BACKGROUNDS = "overlay-backgrounds";

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

  public boolean generateOverlay() throws Exception {
    return onOverlayGeneration();
  }

  private boolean onOverlayGeneration() throws Exception {
    try {
      Config.getOverlayGeneratorConfig().reload();
      BufferedImage bufferedImage = new OverlayGraphics(this, gameService, highscoreService).draw();
      if (bufferedImage != null) {
        ImageUtil.write(bufferedImage, getOverlayFile());
        return true;
      }
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
    return false;
  }

  public List<String> getBackgrounds() {
    File[] files = getOverlayBackgroundsFolder().listFiles((dir, name) -> name.endsWith("jpg") || name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  public File getOverlayBackgroundsFolder() {
    return new File(SystemService.RESOURCES, OVERLAY_BACKGROUNDS);
  }

  private File getOverlayFile() {
    return new File(SystemService.RESOURCES, "overlay.jpg");
  }

  @Override
  public void afterPropertiesSet() {
    try {
      onOverlayGeneration();
    } catch (Exception e) {
      LOG.info("Initial overlay generation failed: " + e.getMessage(), e);
    }

    new Thread(() -> {
      OverlayWindowFX.systemService = systemService;
      OverlayWindowFX.preferencesService = preferencesService;
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();
  }
}
