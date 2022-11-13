package de.mephisto.vpin.server.overlay;

import de.mephisto.vpin.server.overlay.fx.OverlayWindowFX;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OverlayService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      OverlayWindowFX.systemService = systemService;
      OverlayWindowFX.preferencesService = preferencesService;
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();
  }
}
